package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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
            sender.sendMessage("§cUso: /warpcreate <nombre> <tipo> <slot> <permiso>");
            return true;
        }

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
        warpsConfig.set(path + ".LORE", java.util.Arrays.asList("&7", "&e➦ Click to teleport"));
        warpsConfig.set(path + ".PERMISSION-REQUIRED", true);
        warpsConfig.set(path + ".PERMISSION", permission);
        warpsConfig.set(path + ".warp", type.equals("warp"));
        warpsConfig.set(path + ".command", type.equals("command"));

        // Guardar la configuración
        plugin.saveCustomConfig("warps.yml");

        sender.sendMessage(MessageUtils.getColoredMessage("&aEl warp '" + warpName + "' ha sido creado correctamente."));
        return true;
    }
}