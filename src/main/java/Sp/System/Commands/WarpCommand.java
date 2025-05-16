package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpCommand implements CommandExecutor {

    private final SystemPlugin plugin;
    private final Map<UUID, Long> warpCooldowns = new HashMap<>();

    public WarpCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration warpsConfig = plugin.getCustomConfig("warps.yml");

        if (args.length == 0) {
            openWarpInventory(player, warpsConfig);
            return true;
        }

        String warpName = args[0].toLowerCase();

        if (isInCooldown(player)) {
            long timeLeft = getCooldownTimeLeft(player);
            player.sendMessage(MessageUtils.getColoredMessage("&cDebes esperar " + timeLeft + " segundos antes de usar otro warp."));
            return true;
        }

        String matchedWarp = warpsConfig.getConfigurationSection("ITEMS").getKeys(false).stream()
                .filter(key -> key.equalsIgnoreCase(warpName))
                .findFirst()
                .orElse(null);

        if (matchedWarp == null) {
            player.sendMessage(MessageUtils.getColoredMessage("&cEl warp especificado no existe."));
            return true;
        }

        boolean isWarp = warpsConfig.getBoolean("ITEMS." + matchedWarp + ".warp", false);
        boolean isCommand = warpsConfig.getBoolean("ITEMS." + matchedWarp + ".command", false);

        if (isCommand) {
            executeCommands(player, warpsConfig, matchedWarp);
        } else if (isWarp) {
            teleportPlayer(player, warpsConfig, matchedWarp);
        } else {
            player.sendMessage(MessageUtils.getColoredMessage("&cEl warp especificado no está configurado correctamente."));
        }

        applyCooldown(player);
        return true;
    }

    private void openWarpInventory(Player player, FileConfiguration warpsConfig) {
        String inventoryTitle = MessageUtils.getColoredMessage(warpsConfig.getString("TITLE", "Warps"));
        int inventorySize = warpsConfig.getInt("SIZE", 54);

        Inventory warpInventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);

        boolean headDatabaseAvailable = Bukkit.getPluginManager().getPlugin("HeadDatabase") != null;
        HeadDatabaseAPI headDatabaseAPI = headDatabaseAvailable ? new HeadDatabaseAPI() : null;

        for (String warpName : warpsConfig.getConfigurationSection("ITEMS").getKeys(false)) {
            String materialName = warpsConfig.getString("ITEMS." + warpName + ".MATERIAL", "ENDER_PEARL");
            ItemStack item;

            if (materialName.startsWith("HD-") && headDatabaseAvailable) {
                String headID = materialName.substring(3);
                item = headDatabaseAPI.getItemHead(headID);
            } else if (materialName.startsWith("PLAYER-")) {
                String playerName = materialName.substring(7);
                item = getPlayerHead(playerName);
            } else {
                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    continue;
                }
                item = new ItemStack(material);
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessageUtils.getColoredMessage(warpsConfig.getString("ITEMS." + warpName + ".NAME", warpName)));
                meta.setLore(MessageUtils.getColoredMessages(warpsConfig.getStringList("ITEMS." + warpName + ".LORE")));
                if (meta instanceof PersistentDataHolder) {
                    ((PersistentDataHolder) meta).getPersistentDataContainer().set(SystemPlugin.getWarpKey(), PersistentDataType.STRING, warpName);
                }
                item.setItemMeta(meta);
            }

            int slot = warpsConfig.getInt("ITEMS." + warpName + ".SLOT", -1);
            if (slot >= 0 && slot < inventorySize) {
                warpInventory.setItem(slot, item);
            } else {
                warpInventory.addItem(item);
            }
        }

        player.openInventory(warpInventory);
    }

    private ItemStack getPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(playerName);
            meta.setLore(Collections.singletonList("Cabeza de " + playerName));
            ((org.bukkit.inventory.meta.SkullMeta) meta).setOwner(playerName);
            head.setItemMeta(meta);
        }

        return head;
    }

    private void teleportPlayer(Player player, FileConfiguration warpsConfig, String warpName) {
        String worldName = warpsConfig.getString("ITEMS." + warpName + ".WORLD");
        double x = warpsConfig.getDouble("ITEMS." + warpName + ".X");
        double y = warpsConfig.getDouble("ITEMS." + warpName + ".Y");
        double z = warpsConfig.getDouble("ITEMS." + warpName + ".Z");
        float yaw = (float) warpsConfig.getDouble("ITEMS." + warpName + ".YAW");
        float pitch = (float) warpsConfig.getDouble("ITEMS." + warpName + ".PITCH");

        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            player.sendMessage(MessageUtils.getColoredMessage("&cEl mundo especificado no existe o no está cargado."));
            return;
        }

        Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        player.teleport(location);
        player.sendMessage(MessageUtils.getColoredMessage("&a¡Has sido teletransportado al warp " + warpName + "!"));
    }

    private void executeCommands(Player player, FileConfiguration warpsConfig, String warpName) {
        String path = "ITEMS." + warpName;
        for (String cmd : warpsConfig.getStringList(path + ".COMMANDS")) {
            if (cmd.startsWith("{player}")) {
                player.performCommand(cmd.replace("{player} ", ""));
            } else if (cmd.startsWith("{console}")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{console} ", ""));
            }
        }
        player.sendMessage(MessageUtils.getColoredMessage("&a¡Comandos ejecutados para el warp " + warpName + "!"));
    }

    private boolean isInCooldown(Player player) {
        return warpCooldowns.containsKey(player.getUniqueId()) &&
                (System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId())) < plugin.getConfig().getInt("WARP.COOLDOWN", 5) * 1000;
    }

    private long getCooldownTimeLeft(Player player) {
        long cooldown = plugin.getConfig().getInt("WARP.COOLDOWN", 5) * 1000;
        return (cooldown - (System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId()))) / 1000;
    }

    private void applyCooldown(Player player) {
        warpCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}