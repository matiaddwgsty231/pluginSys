package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.PlayerListener.*;
import Sp.System.utils.MessageUtils;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import Sp.System.Manager.ActionBarManager;
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
import org.bukkit.scheduler.BukkitRunnable;

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
            sender.sendMessage("§cSolo los jugadores pueden usar este comando.");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration warpsConfig = plugin.getCustomConfig("warps.yml");

        // Si no se pasa argumento, abrir el menú
        if (args.length == 0) {
            openWarpInventory(player, warpsConfig, "menu1");
            return true;
        }

        // Si se pasa el nombre del warp
        String warpName = args[0];

        if (!warpsConfig.contains("ITEMS." + warpName)) {
            player.sendMessage("§cEl warp especificado no existe.");
            return true;
        }

        if (isInCooldown(player)) {
            player.sendMessage(MessageUtils.getColoredMessage("&cEspera " + getCooldownTimeLeft(player) + " segundos para usar otro warp."));
            return true;
        }

        String path = "ITEMS." + warpName;
        boolean permissionRequired = warpsConfig.getBoolean(path + ".PERMISSION-REQUIRED", false);
        String permission = warpsConfig.getString(path + ".PERMISSION", "");

        if (permissionRequired && !player.hasPermission(permission)) {
            player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.NOPERM", "&cNo tienes permiso para usar este warp.")));
            return true;
        }

        boolean isCommandWarp = warpsConfig.getBoolean(path + ".command", false);
        if (isCommandWarp) {
            executeCommands(player, warpsConfig, warpName);
        } else {
            teleportPlayer(player, warpsConfig, warpName, plugin.getTeleportCancelListener(), new ActionBarManager(plugin));
        }

        applyCooldown(player);
        return true;
    }

    public void openWarpInventory(Player player, FileConfiguration warpsConfig, String menu) {
        String inventoryTitle = MessageUtils.getColoredMessage(warpsConfig.getString("TITLE", "Warps"));
        int inventorySize = warpsConfig.getInt("SIZE", 54);

        Inventory warpInventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);

        boolean headDatabaseAvailable = Bukkit.getPluginManager().getPlugin("HeadDatabase") != null;
        HeadDatabaseAPI headDatabaseAPI = headDatabaseAvailable ? new HeadDatabaseAPI() : null;

        // Mostrar LINKS (navegación entre menús)
        if (warpsConfig.contains("LINK")) {
            for (String key : warpsConfig.getConfigurationSection("LINK").getKeys(false)) {
                String menuKey = "LINK." + key;
                if (!menu.equalsIgnoreCase(warpsConfig.getString(menuKey + ".MENU"))) continue;

                String materialName = warpsConfig.getString(menuKey + ".MATERIAL", "ENDER_PEARL");
                ItemStack item = getItemFromMaterial(materialName, headDatabaseAPI);

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.getColoredMessage(warpsConfig.getString(menuKey + ".NAME", key)));
                    meta.setLore(MessageUtils.getColoredMessages(warpsConfig.getStringList(menuKey + ".LORE")));
                    if (meta instanceof PersistentDataHolder) {
                        ((PersistentDataHolder) meta).getPersistentDataContainer().set(SystemPlugin.getWarpKey(), PersistentDataType.STRING, "menu:" + warpsConfig.getString(menuKey + ".GO"));
                    }
                    item.setItemMeta(meta);
                }

                int slot = warpsConfig.getInt(menuKey + ".SLOT", -1);
                if (slot >= 0 && slot < inventorySize) {
                    warpInventory.setItem(slot, item);
                } else {
                    warpInventory.addItem(item);
                }
            }
        }

        // Mostrar warps del menú actual
        if (!warpsConfig.contains("ITEMS")) {
            player.sendMessage("§cNo hay warps configurados.");
            return;
        }

        for (String warpName : warpsConfig.getConfigurationSection("ITEMS").getKeys(false)) {
            String path = "ITEMS." + warpName;
            if (!menu.equalsIgnoreCase(warpsConfig.getString(path + ".MENU", "menu1"))) continue;

            String materialName = warpsConfig.getString(path + ".MATERIAL", "ENDER_PEARL");
            ItemStack item = getItemFromMaterial(materialName, headDatabaseAPI);

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessageUtils.getColoredMessage(warpsConfig.getString(path + ".NAME", warpName)));
                meta.setLore(MessageUtils.getColoredMessages(warpsConfig.getStringList(path + ".LORE")));
                if (meta instanceof PersistentDataHolder) {
                    ((PersistentDataHolder) meta).getPersistentDataContainer().set(SystemPlugin.getWarpKey(), PersistentDataType.STRING, warpName);
                }
                item.setItemMeta(meta);
            }

            int slot = warpsConfig.getInt(path + ".SLOT", -1);
            if (slot >= 0 && slot < inventorySize) {
                warpInventory.setItem(slot, item);
            } else {
                warpInventory.addItem(item);
            }
        }

        player.openInventory(warpInventory);
    }

    private ItemStack getItemFromMaterial(String materialName, HeadDatabaseAPI headDatabaseAPI) {
        if (materialName.startsWith("HD-") && headDatabaseAPI != null) {
            return headDatabaseAPI.getItemHead(materialName.substring(3));
        } else if (materialName.startsWith("PLAYER-")) {
            return getPlayerHead(materialName.substring(7));
        } else {
            Material mat = Material.matchMaterial(materialName);
            return mat != null ? new ItemStack(mat) : new ItemStack(Material.BARRIER);
        }
    }

    private ItemStack getPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
            ((org.bukkit.inventory.meta.SkullMeta) meta).setOwner(playerName);
        }
        if (meta != null) {
            meta.setDisplayName("Cabeza de " + playerName);
            meta.setLore(Collections.singletonList("Cabeza de " + playerName));
            head.setItemMeta(meta);
        }
        return head;
    }

    // Para teletransportar
    public void teleportPlayer(Player player, FileConfiguration warpsConfig, String warpName, TeleportCancelListener cancelListener, ActionBarManager actionBarManager) {
        String path = "ITEMS." + warpName;
        String worldName = warpsConfig.getString(path + ".WORLD");
        double x = warpsConfig.getDouble(path + ".X");
        double y = warpsConfig.getDouble(path + ".Y");
        double z = warpsConfig.getDouble(path + ".Z");
        float yaw = (float) warpsConfig.getDouble(path + ".YAW");
        float pitch = (float) warpsConfig.getDouble(path + ".PITCH");

        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            return;
        }

        int countdownTime = warpsConfig.getInt("WARP.TELEPORT_DELAY", 10); // Tiempo de cuenta atrás en segundos
        player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.TELEPORTING", "&eSerás teletransportado al warp {warp} en {time} segundos...")
                .replace("{warp}", warpName)
                .replace("{time}", String.valueOf(countdownTime))));
        cancelListener.addTeleportingPlayer(player.getUniqueId());

        new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                if (!cancelListener.teleportingPlayers.contains(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                    player.teleport(location);
                    player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.TELEPORTED", "&a¡Has sido teletransportado al warp {warp}!")
                            .replace("{warp}", warpName)));
                    cancelListener.removeTeleportingPlayer(player.getUniqueId());
                    cancel();
                    return;
                }

                // Mostrar mensaje en la barra de acción usando ActionBarManager
                String actionBarMessage = warpsConfig.getString("WARP.ACTIONBAR", "&eTeletransporte en &c{time} &esegundos...");
                actionBarMessage = actionBarMessage.replace("{time}", String.valueOf(timeLeft));
                actionBarManager.sendActionBarToPlayer(player, MessageUtils.getColoredMessage(actionBarMessage));
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Ejecutar cada segundo
    }

    public void executeCommands(Player player, FileConfiguration warpsConfig, String warpName) {
        String path = "ITEMS." + warpName;
        for (String cmd : warpsConfig.getStringList(path + ".COMMANDS")) {
            cmd = cmd.replace("%player%", player.getName());

            if (cmd.startsWith("{player}")) {
                String commandToRun = cmd.replace("{player} ", "");
                player.chat("/" + commandToRun); // Ejecuta el comando como jugador
            } else if (cmd.startsWith("{console}")) {
                String commandToRun = cmd.replace("{console} ", "");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToRun); // Ejecuta el comando como consola
            }
        }
    }


    public boolean isInCooldown(Player player) {
        return warpCooldowns.containsKey(player.getUniqueId()) &&
                (System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId())) < plugin.getCustomConfig("warps.yml").getInt("WARP.COOLDOWN", 5) * 1000;
    }

    public long getCooldownTimeLeft(Player player) {
        long cooldown = plugin.getCustomConfig("warps.yml").getInt("WARP.COOLDOWN", 5) * 1000;
        return (cooldown - (System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId()))) / 1000;
    }

    public void applyCooldown(Player player) {
        warpCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
