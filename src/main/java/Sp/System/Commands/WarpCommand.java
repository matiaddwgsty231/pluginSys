package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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
            // Abrir el inventario de warps
            openWarpInventory(player, warpsConfig);
            return true;
        }

        String warpName = args[0].toLowerCase(); // Convertir a minúsculas

        // Verificar cooldown
        if (isInCooldown(player)) {
            long timeLeft = getCooldownTimeLeft(player);
            player.sendMessage(MessageUtils.getColoredMessage("&cDebes esperar " + timeLeft + " segundos antes de usar otro warp."));
            return true;
        }

        // Buscar el warp ignorando mayúsculas y minúsculas
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

        // Aplicar cooldown
        applyCooldown(player);
        return true;
    }

    private void openWarpInventory(Player player, FileConfiguration warpsConfig) {
        // Implementa la lógica para abrir el inventario de warps
        String inventoryTitle = MessageUtils.getColoredMessage(warpsConfig.getString("TITLE", "Warps"));
        player.sendMessage("§aAbriendo el inventario: " + inventoryTitle); // Mensaje temporal
        // Aquí deberías abrir el inventario personalizado
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