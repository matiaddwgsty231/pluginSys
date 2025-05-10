package Sp.System.utils;

import Sp.System.SystemPlugin;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {
    public static String getColoredMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getprefix(String message) {
        return SystemPlugin.prefix + message;
    }

    public static List<String> getColoredMessages(List<String> stringList) {
        return stringList.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }
    public static String stripColor(String input) {
        return input.replaceAll("(?i)§[0-9A-FK-OR]", "");
    }
}