package net.cubiness.ranks;

import org.bukkit.ChatColor;

public class Rank {
  private String name;
  private String displayName;
  private ChatColor color;

  public Rank() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public ChatColor getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = ChatColor.valueOf(color);
  }
}
