package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.Arrays;

public class WarpCreateCommand implements CommandExecutor {

    private final SystemPlugin plugin;

    public WarpCreateCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§cUso: /warpcreate <nombre> <warp/command> <slot> <permiso>");
            return true;
        }

        Player player = (Player) sender;
        String warpName = args[0];
        String type = args[1].toLowerCase(); // "warp" o "command"
        int slot;
        try {
            slot = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cEl slot debe ser un número.");
            return true;
        }
        String permission = args[3];

        FileConfiguration warpsConfig = plugin.getCustomConfig("warps.yml");

        // Crear la configuración del nuevo warp
        String path = "ITEMS." + warpName;
        warpsConfig.set(path + ".MATERIAL", "ENDER_PEARL");
        warpsConfig.set(path + ".SLOT", slot);
        warpsConfig.set(path + ".NAME", "&7&l" + warpName);
        warpsConfig.set(path + ".LORE", Arrays.asList("&7", "&e➦ Click to teleport"));
        warpsConfig.set(path + ".PERMISSION-REQUIRED", true);
        warpsConfig.set(path + ".PERMISSION", permission);

        if (type.equals("warp")) {
            warpsConfig.set(path + ".warp", true);
            warpsConfig.set(path + ".command", false);

            // Obtener la ubicación del jugador
            Location location = player.getLocation();
            warpsConfig.set(path + ".WORLD", location.getWorld().getName());
            warpsConfig.set(path + ".X", location.getX());
            warpsConfig.set(path + ".Y", location.getY());
            warpsConfig.set(path + ".Z", location.getZ());
            warpsConfig.set(path + ".YAW", location.getYaw());
            warpsConfig.set(path + ".PITCH", location.getPitch());
        } else if (type.equals("command")) {
            warpsConfig.set(path + ".warp", false);
            warpsConfig.set(path + ".command", true);

            // Agregar comandos predeterminados
            warpsConfig.set(path + ".COMMANDS", Arrays.asList("{console} command", "{player} command"));
        } else {
            sender.sendMessage("§cEl tipo debe ser 'warp' o 'command'.");
            return true;
        }

        // Guardar la configuración
        plugin.saveCustomConfig("warps.yml");

        sender.sendMessage(MessageUtils.getColoredMessage("&aEl warp '" + warpName + "' ha sido creado correctamente."));
        return true;
    }
}