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

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider("ranks-table"))
            .build();
    DynamoDB dynamoDB = new DynamoDB(client);
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
