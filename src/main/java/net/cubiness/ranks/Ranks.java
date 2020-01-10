package net.cubiness.ranks;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class Ranks extends JavaPlugin implements Listener {
  private Table playerRanks;
  private DynamoDB dbClient;
  private AmazonS3 s3Client;
  private String bucketName;
  private HashMap<String, Rank> ranks = new HashMap<>();
  private Map<Player, PermissionAttachment> perms = new HashMap<>();

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    String profileName = (String) getConfig().get("aws.profile");
    String tableName = (String) getConfig().get("aws.table");
    bucketName = (String) getConfig().get("aws.bucket");

    if (profileName == null || profileName.equals("PROFILE")) {
      getLogger().severe("Please set aws.profile in config.yml to a valid user in ~/.aws/credentials");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (tableName == null || tableName.equals("TABLE")) {
      getLogger().severe("Please set aws.table in config.yml to a valid table in aws");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (bucketName == null || bucketName.equals("BUCKET")) {
      getLogger().severe("Please set aws.bucket in config.yml to a valid bucket in aws");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    AmazonDynamoDB dynamodb = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider(profileName))
            .build();
    dbClient = new DynamoDB(dynamodb);
    playerRanks = dbClient.getTable(tableName);
    s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider(profileName))
            .build();
    updateAllRanks();
    refreshPlayerNames();
    refreshPlayerRanks();
    getLogger().info("Ranks plugin loaded!");
  }

  @Override
  public void onDisable() {
    dbClient.shutdown();
    getLogger().info("Ranks plugin disabled");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (label.equals("rank")) {
      Item item = playerRanks.getItem("username", sender.getName());
      sender.sendMessage(ChatColor.GOLD + "Rank: " + ChatColor.WHITE + item.get("rank"));
    } else if (label.equals("rankset")) {
      if (sender instanceof Player) {
        if (args.length == 1) {
          playerRanks.putItem(new Item()
                  .withPrimaryKey("username", sender.getName())
                  .withString("rank", args[0]));
          updateName((Player) sender);
        } else if (args.length == 2) {
          playerRanks.putItem(new Item()
                  .withPrimaryKey("username", args[1])
                  .withString("rank", args[0]));
          updateName(Bukkit.getPlayer(args[1]));
        }
      } else {
        sender.sendMessage("Only players can run this command!");
      }
    } else if (label.equals("refreshranks")) {
      if (sender instanceof Player) {
        updateAllRanks();
        sender.sendMessage(ChatColor.YELLOW + "Loaded " + ranks.size() + " ranks");
      } else {
        sender.sendMessage("Only players can run this command!");
      }
    } else {
      return false;
    }
    return true;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    updateName(p);
    updatePermissions(p);
  }

  private void refreshPlayerRanks() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      updatePermissions(p);
    }
  }

  private void updatePermissions(Player p) {
    if (!perms.containsKey(p)) {
      PermissionAttachment perm = p.addAttachment(this);
      perms.put(p, perm);
    }
    PermissionAttachment perm = perms.get(p);
    Rank rank = getRank(p);
    Map<String, Boolean> permMap = rank.getPermissions();
    getLogger().info("Player perms: " + permMap);
    Set<Pattern> trueMatchers = new HashSet<>();
    Set<Pattern> falseMatchers = new HashSet<>();
    permMap.forEach((str, bool) -> {
      str = str.replaceAll("\\*", ".*");
      Pattern reg = Pattern.compile(str);
      if (bool) {
        trueMatchers.add(reg);
      } else {
        falseMatchers.add(reg);
      }
    });
    getAllPermissions().forEach(serverPerm -> {
      trueMatchers.forEach(reg -> {
        if (reg.matcher(serverPerm).matches()) {
          getLogger().info("Setting true: " + serverPerm);
          perm.setPermission(serverPerm, true);
        }
      });
      falseMatchers.forEach(reg -> {
        if (reg.matcher(serverPerm).matches()) {
          getLogger().info("Setting false: " + serverPerm);
          perm.setPermission(serverPerm, false);
        }
      });
    });
    p.recalculatePermissions();
    p.updateCommands();
  }

  private List<String> getAllPermissions() {
    List<String> perms = new ArrayList<>();
    Arrays.asList(Bukkit.getServer().getPluginManager().getPlugins()).forEach(plugin -> {
      plugin.getDescription().getPermissions().forEach(perm -> {
        getLogger().info("Found perm: " + perm.getName());
        perms.add(perm.getName());
      });
    });
    return perms;
  }

  private void refreshPlayerNames() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      updateName(p);
    }
  }

  private void updateName(Player p) {
    Rank rank = getRank(p);
    if (rank != null) {
      getLogger().info("Player " + p.getName() + " has rank " + rank.getName());
      String displayName = rank.getColor() +
              "[" +
              rank.getDisplayName() +
              "] " +
              p.getName() +
              ChatColor.WHITE;
      p.setDisplayName(displayName);
      p.setPlayerListName(displayName);
    } else {
      getLogger().warning("Player " + p.getName() + " has an invalid rank!");
    }
  }

  private void updateAllRanks() {
    ObjectListing list = s3Client.listObjects(bucketName, "ranks/");
    List<S3ObjectSummary> objects = list.getObjectSummaries();
    for (S3ObjectSummary os : objects) {
      String key = os.getKey();
      String[] sections = key.split("/");
      if (sections.length == 1) {
        continue;
      }
      String name = sections[1];
      // for ranks/ key
      if (name.equals("")) {
        continue;
      }
      name = name.split("\\.")[0];
      updateRank(name);
    }
    getLogger().info("Loaded ranks: " + ranks);
  }

  private void updateRank(String name) {
    S3Object obj = s3Client.getObject(bucketName, "ranks/" + name + ".cfg");
    InputStream stream = obj.getObjectContent();
    Scanner sc = new Scanner(stream);
    StringBuilder sb = new StringBuilder();
    while (sc.hasNext()) {
      String line = sc.nextLine();
      sb.append(line);
      sb.append("\n");
    }
    List<String> sections = Arrays.asList(sb.toString().split(";"));
    HashMap<String, String> data = new HashMap<>();
    sections.forEach(section -> {
      if (!section.trim().isEmpty()) {
        List<String> keyVal = Arrays.asList(section.split("="));
        if (keyVal.size() != 2) {
          getLogger().severe("Error in config file for rank " + name);
          getLogger().severe("Line: \"" + section + "\"");
          return;
        }
        data.put(keyVal.get(0).trim(), keyVal.get(1).trim());
      }
    });
    Rank rank = new Rank();
    rank.setName(name);
    rank.setDisplayName(data.get("displayName"));
    rank.setColor(data.get("color"));
    String rawPermissions = data.get("permissions");
    Map<String, Boolean> perms = new HashMap<>();
    Arrays.asList(rawPermissions.split(",")).forEach(perm -> {
      perm = perm.trim();
      if (perm.startsWith("-")) {
        perms.put(perm.substring(1), false);
      } else {
        perms.put(perm, true);
      }
    });
    rank.setPermissions(perms);
    ranks.put(name, rank);
  }

  private Rank getRank(Player p) {
    Item item = playerRanks.getItem("username", p.getName());
    String name;
    if (item == null) {
      name = "citizen";
    } else {
      name = (String) item.get("rank");
    }
    return ranks.getOrDefault(name, null);
  }
}
