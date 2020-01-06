package net.cubiness.ranks;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.nio.ByteBufferAsFloatBufferL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Ranks extends JavaPlugin implements Listener {
  private Table playerRanks;
  private DynamoDB dbClient;
  private AmazonS3 s3Client;
  private String bucketName;
  private HashMap<String, Rank> ranks = new HashMap<>();

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    String profileName = (String) getConfig().get("aws.profile");
    String tableName = (String) getConfig().get("aws.table");
    bucketName = (String) getConfig().get("aws.bucket");

    if (profileName == null || profileName.equals("PROFILE")) {
      getLogger().info("Please set aws.profile in config.yml to a valid user in ~/.aws/credentials");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (tableName == null || tableName.equals("TABLE")) {
      getLogger().severe("Please set aws.table in config.yml to a valid table in aws");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (bucketName == null || bucketName.equals("BUCKET")) {
      getLogger().info("Please set aws.bucket in config.yml to a valid bucket in aws");
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
      return true;
    } else if (label.equals("rankset")) {
      if (args.length == 1) {
        playerRanks.putItem(new Item()
                .withPrimaryKey("username", sender.getName())
                .withString("rank", args[0]));
        return true;
      } else if (args.length == 2) {
        playerRanks.putItem(new Item()
                .withPrimaryKey("username", args[1])
                .withString("rank", args[0]));
        return true;
      }
    }
    return false;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    updateName(p);
  }

  private void updateName(Player p) {

  }

  private void updateRank(String name) {
    S3Object obj = s3Client.getObject(bucketName, "ranks/" + name + ".cfg");
    InputStream stream = obj.getObjectContent();
    Scanner sc = new Scanner(stream);
    HashMap<String, String> data = new HashMap<>();
    int lineNum = 0;
    while (sc.hasNext()) {
      String line = sc.nextLine();
      if (line.equals("")) {
        lineNum++;
        continue;
      }
      List<String> sections = Arrays.asList(line.split("="));
      if (sections.size() != 2) {
        getLogger().severe("Error on line " + lineNum + " of rank " + name);
        return;
      }
      data.put(sections.get(0), sections.get(1));
    }
    Rank rank = new Rank();
    rank.setName(name);
    rank.setDisplayName(data.get("displayName"));
    rank.setColor(data.get("color"));
    ranks.put(name, rank);
  }

  private String getRank(Player p) {
    Item item = playerRanks.getItem("username", p.getName());
    return (String) item.get("rank");
  }
}
