package net.cubiness.ranks;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
  private Table ranks;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_WEST_2)
            .build();
    getLogger().info("Client: " + client);
    DynamoDB dynamoDB = new DynamoDB(client);
    getLogger().info("Db: " + dynamoDB);
    ranks = dynamoDB.getTable("cubiness-ranks");
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
