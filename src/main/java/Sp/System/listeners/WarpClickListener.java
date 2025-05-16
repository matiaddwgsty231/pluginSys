package Sp.System.listeners;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpClickListener implements Listener {

    private final SystemPlugin plugin;
    private final FileConfiguration warpsConfig;
    private final Map<UUID, Long> warpCooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> teleportingPlayers = new HashMap<>();

    public WarpClickListener(SystemPlugin plugin) {
        this.plugin = plugin;
        this.warpsConfig = plugin.getCustomConfig("warps.yml");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getInventory(); // Cambiado a getInventory()
        if (clickedInventory == null || event.getCurrentItem() == null) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
            return;
        }

        String inventoryTitle = event.getView().getTitle();
        String configuredTitle = MessageUtils.getColoredMessage(warpsConfig.getString("TITLE", "Warps"));

        if (!inventoryTitle.equals(configuredTitle)) {
            return;
        }

        event.setCancelled(true);

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta instanceof PersistentDataHolder) {
            String warpKey = ((PersistentDataHolder) meta).getPersistentDataContainer()
                    .get(SystemPlugin.getWarpKey(), PersistentDataType.STRING);

            if (warpKey == null || !warpsConfig.contains("ITEMS." + warpKey)) {
                return;
            }

            Player player = (Player) event.getWhoClicked();

            if (warpsConfig.getBoolean("ITEMS." + warpKey + ".PERMISSION-REQUIRED") &&
                    !player.hasPermission(warpsConfig.getString("ITEMS." + warpKey + ".PERMISSION"))) {
                player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.NoPerm")));
                return;
            }

            boolean isCommand = warpsConfig.getBoolean("ITEMS." + warpKey + ".command", false);
            boolean isWarp = warpsConfig.getBoolean("ITEMS." + warpKey + ".warp", false);

            if (isCommand) {
                executeCommands(player, warpKey);
            } else if (isWarp) {
                if (isInCooldown(player)) {
                    long timeLeft = getCooldownTimeLeft(player);
                    player.sendMessage(MessageUtils.getColoredMessage("&cDebes esperar " + timeLeft + " segundos antes de usar otro warp."));
                    return;
                }

                teleportPlayer(player, warpsConfig, warpKey);
                applyCooldown(player);
            }
        }
    }

    private void executeCommands(Player player, String warpKey) {
        for (String cmd : warpsConfig.getStringList("ITEMS." + warpKey + ".COMMANDS")) {
            if (cmd.startsWith("{player}")) {
                player.performCommand(cmd.replace("{player} ", ""));
            } else if (cmd.startsWith("{console}")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{console} ", ""));
            }
        }
    }

    private void teleportPlayer(Player player, FileConfiguration warpsConfig, String warpName) {
        int delay = plugin.getConfig().getInt("WARP.TELEPORT_DELAY", 5);

        player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.TELEPORTING", "&aTeleporting in {time} seconds...")
                .replace("{time}", String.valueOf(delay))));

        Location initialLocation = player.getLocation();

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                teleportingPlayers.remove(player.getUniqueId());

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
                player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.TELEPORTED", "&aSuccessfully teleported to &a&l{warp}&a.")
                        .replace("{warp}", warpName)));
            }
        }.runTaskLater(plugin, delay * 20L);

        teleportingPlayers.put(player.getUniqueId(), task);
    }

    private boolean isInCooldown(Player player) {
        return warpCooldowns.containsKey(player.getUniqueId()) &&
                (System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId())) < warpsConfig.getInt("WARP.COOLDOWN", 5) * 1000;
    }

    private long getCooldownTimeLeft(Player player) {
        long cooldown = warpsConfig.getInt("WARP.COOLDOWN", 5) * 1000;
        return (cooldown - (System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId()))) / 1000;
    }

    private void applyCooldown(Player player) {
        warpCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}