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
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpClickListener implements Listener {

    private final FileConfiguration warpsConfig;
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final Map<UUID, Long> combatTimers = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> warpCooldowns = new HashMap<>();

    public WarpClickListener(SystemPlugin plugin) {
        this.warpsConfig = plugin.getCustomConfig("warps.yml");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
            return;
        }

        String inventoryTitle = event.getView().getTitle();
        String configuredTitle = MessageUtils.getColoredMessage(warpsConfig.getString("TITLE", "Warps"));

        if (!inventoryTitle.equals(configuredTitle)) {
            return; // No es el inventario de warps
        }

        event.setCancelled(true); // Evita que el jugador saque ítems del inventario

        String warpName = clickedItem.getItemMeta().getDisplayName();
        warpName = MessageUtils.stripColor(warpName); // Elimina colores para obtener el nombre limpio

        if (!warpsConfig.contains("ITEMS." + warpName)) {
            return; // El warp no existe
        }

        Player player = (Player) event.getWhoClicked();

        // Verificar cooldown
        if (isInCooldown(player)) {
            long timeLeft = getCooldownTimeLeft(player);
            player.sendMessage(MessageUtils.getColoredMessage("&cDebes esperar " + timeLeft + " segundos antes de usar otro warp."));
            return;
        }

        // Verificar permisos
        if (warpsConfig.getBoolean("ITEMS." + warpName + ".PERMISSION-REQUIRED") &&
                !player.hasPermission(warpsConfig.getString("ITEMS." + warpName + ".PERMISSION"))) {
            player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.NoPerm")));
            return; // Cancela cualquier acción relacionada con este warp
        }

        boolean isWarp = warpsConfig.getBoolean("ITEMS." + warpName + ".warp", false);
        boolean isCommand = warpsConfig.getBoolean("ITEMS." + warpName + ".command", false);

        if (isCommand) {
            executeCommands(player, warpName);
        } else if (isWarp) {
            if (isPlayerInCombat(player)) {
                String combatMessage = warpsConfig.getString("WARP.COMBAT", "&cYou can't use warps while in combat.");
                player.sendMessage(MessageUtils.getColoredMessage(combatMessage.replace("{combat}", getCombatTimeLeft(player))));
                return;
            }

            if (isPlayerTeleporting(player)) {
                player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.TELEPORTING-ALREADY")));
                return;
            }

            startTeleport(player, warpName);
        }

        // Aplicar cooldown
        applyCooldown(player);
    }

    private void startTeleport(Player player, String warpName) {
        int cooldown = warpsConfig.getInt("WARP.COOLDOWN", 5);
        String teleportingMessage = warpsConfig.getString("WARP.TELEPORTING", "&aTeleporting in {time} seconds...");
        player.sendMessage(MessageUtils.getColoredMessage(teleportingMessage.replace("{time}", String.valueOf(cooldown))));

        lastLocations.put(player.getUniqueId(), player.getLocation());

        Bukkit.getScheduler().runTaskLater(SystemPlugin.getInstance(), () -> {
            if (player.isOnline() && !hasPlayerMoved(player)) {
                teleportPlayer(player, warpName);
                String teleportedMessage = warpsConfig.getString("WARP.TELEPORTED", "&aSuccessfully teleported to {warp}.");
                player.sendMessage(MessageUtils.getColoredMessage(teleportedMessage.replace("{warp}", warpName)));
            } else {
                player.sendMessage(MessageUtils.getColoredMessage(warpsConfig.getString("WARP.CANCEL")));
            }
        }, cooldown * 20L);
    }

    private void teleportPlayer(Player player, String warpName) {
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

    private void executeCommands(Player player, String warpName) {
        String path = "ITEMS." + warpName;
        for (String cmd : warpsConfig.getStringList(path + ".COMMANDS")) {
            if (cmd.startsWith("{player}")) {
                player.performCommand(cmd.replace("{player} ", ""));
            } else if (cmd.startsWith("{console}")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{console} ", ""));
            }
        }
    }

    private boolean isPlayerInCombat(Player player) {
        return combatTimers.containsKey(player.getUniqueId()) &&
                (System.currentTimeMillis() - combatTimers.get(player.getUniqueId())) < 10000; // 10 segundos de combate
    }

    private String getCombatTimeLeft(Player player) {
        long timeLeft = 10000 - (System.currentTimeMillis() - combatTimers.get(player.getUniqueId()));
        return String.valueOf(timeLeft / 1000);
    }

    private boolean isPlayerTeleporting(Player player) {
        return teleportCooldowns.containsKey(player.getUniqueId()) &&
                (System.currentTimeMillis() - teleportCooldowns.get(player.getUniqueId())) < 5000; // 5 segundos de cooldown
    }

    private boolean hasPlayerMoved(Player player) {
        Location lastLocation = lastLocations.get(player.getUniqueId());
        if (lastLocation == null) {
            return true; // Si no hay una ubicación registrada, asumimos que se movió
        }

        Location currentLocation = player.getLocation();

        // Tolerancia para evitar detecciones falsas
        double tolerance = 0.1;

        return Math.abs(lastLocation.getX() - currentLocation.getX()) > tolerance ||
                Math.abs(lastLocation.getY() - currentLocation.getY()) > tolerance ||
                Math.abs(lastLocation.getZ() - currentLocation.getZ()) > tolerance;
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