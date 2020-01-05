package net.cubiness.ranks;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
  private Table ranks;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    String profileName = (String) getConfig().get("aws.profile");
    String tableName = (String) getConfig().get("aws.table");

    if (profileName == null) {
      getLogger().info("Please set aws.profile in config.yml to a valid config in ~/.aws/credentials");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (tableName == null) {
      getLogger().info("Please set aws.table in config.yml to a valid table in aws");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider(profileName))
            .build();
    DynamoDB dynamoDB = new DynamoDB(client);
    ranks = dynamoDB.getTable(tableName);
    getLogger().info("Ranks plugin loaded!");
  }

  @Override
  public void onDisable() {
    getLogger().info("Ranks plugin disabled");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (label.equals("rank")) {
      // Item item = ranks.getItem("free", 101);
      sender.sendMessage(ranks + "");
      return true;
    }
    return false;
  }
}
