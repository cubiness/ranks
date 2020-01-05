package net.cubiness.ranks;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
  private String access_key;
  private String secret_key;
  private Table ranks;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    Bukkit.broadcastMessage("Ranks plugin loaded!");
    access_key = getConfig().getString("aws.key.access");
    secret_key = getConfig().getString("aws.key.secret");
    if (access_key == null || secret_key == null) {
      saveDefaultConfig();
      access_key = getConfig().getString("aws.key.access");
      secret_key = getConfig().getString("aws.key.secret");
    }
    BasicAWSCredentials awsCreds = new BasicAWSCredentials(access_key, secret_key);

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials((new AWSStaticCredentialsProvider(awsCreds)))
            .build();
    DynamoDB dynamoDB = new DynamoDB(client);
    ranks = dynamoDB.getTable("minecraft");
  }

  @Override
  public void onDisable() {
    Bukkit.broadcastMessage("Ranks plugin disabled");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (label.equals("rank")) {
      // Item item = ranks.getItem("free", 101);
      sender.sendMessage(access_key);
      sender.sendMessage(ranks + "");
      return true;
    }
    return false;
  }
}
