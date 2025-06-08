package Sp.System.Manager;

import Sp.System.SystemPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class WarpCreateManager {

    private final SystemPlugin plugin;

    public WarpCreateManager(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean createWarp(String warpName, String type, int slot, Location location) {
        FileConfiguration warpsConfig = plugin.getCustomConfig("warps.yml");

        String path = "ITEMS." + warpName;
        warpsConfig.set(path + ".MATERIAL", "ENDER_PEARL");
        warpsConfig.set(path + ".SLOT", slot);
        warpsConfig.set(path + ".MENU", "menu1");
        warpsConfig.set(path + ".NAME", "&7&l" + warpName);
        warpsConfig.set(path + ".LORE", Arrays.asList("&7", "&e➦ Click to teleport"));
        warpsConfig.set(path + ".PERMISSION-REQUIRED", false);
        warpsConfig.set(path + ".PERMISSION", "warp." + warpName);

        if (type.equals("warp")) {
            warpsConfig.set(path + ".warp", true);
            warpsConfig.set(path + ".command", false);

            warpsConfig.set(path + ".WORLD", location.getWorld().getName());
            warpsConfig.set(path + ".X", location.getX());
            warpsConfig.set(path + ".Y", location.getY());
            warpsConfig.set(path + ".Z", location.getZ());
            warpsConfig.set(path + ".YAW", location.getYaw());
            warpsConfig.set(path + ".PITCH", location.getPitch());
        } else if (type.equals("command")) {
            warpsConfig.set(path + ".warp", false);
            warpsConfig.set(path + ".command", true);

            warpsConfig.set(path + ".COMMANDS", Arrays.asList("{console} command", "{player} command"));
        } else {
            return false;
        }

        plugin.saveCustomConfig("warps.yml");
        return true;
    }
}