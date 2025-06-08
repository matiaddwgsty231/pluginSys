package Sp.System.utils;

import Sp.System.SystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionBarManager {

    private final SystemPlugin plugin;

    public ActionBarManager(SystemPlugin plugin) {
        this.plugin = plugin;
    }


    public void sendActionBarToPlayer(Player player, String message) {
        if (player != null && message != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }

    public void sendActionBarToAll(String message) {
        if (message != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        }
    }
}