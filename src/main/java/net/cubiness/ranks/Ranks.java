package net.cubiness.ranks;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Ranks extends JavaPlugin implements Listener {
  private Table ranks;
  private DynamoDB dbClient;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    String profileName = (String) getConfig().get("aws.profile");
    String tableName = (String) getConfig().get("aws.table");

    if (profileName == null || profileName.equals("PROFILE")) {
      getLogger().info("Please set aws.profile in config.yml to a valid config in ~/.aws/credentials");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (tableName == null || tableName.equals("TABLE")) {
      getLogger().info("Please set aws.table in config.yml to a valid table in aws");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider(profileName))
            .build();
    dbClient = new DynamoDB(client);
    ranks = dbClient.getTable(tableName);
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
      Item item = ranks.getItem("username", sender.getName());
      sender.sendMessage(ChatColor.GOLD + "Rank: " + ChatColor.WHITE + item.get("rank"));
      return true;
    } else if (label.equals("rankset")) {
      if (args.length == 1) {
        ranks.putItem(new Item()
                .withPrimaryKey("username", sender.getName())
                .withString("rank", args[0]));
        return true;
      } else if (args.length == 2) {
        ranks.putItem(new Item()
                .withPrimaryKey("username", args[1])
                .withString("rank", args[0]));
        return true;
      }
    }
    return false;
  }
}
