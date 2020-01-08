package net.cubiness.ranks;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class Rank {
  private String name;
  private String displayName;
  private ChatColor color;
  private final Map<String, Boolean> permissions = new HashMap<>();

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

  public Map<String, Boolean> getPermissions() {
    return permissions;
  }

  public void setPermissions(Map<String, Boolean> permissions) {
    this.permissions.clear();
    this.permissions.putAll(permissions);
  }
}
