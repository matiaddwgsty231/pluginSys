package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import Sp.System.Manager.WarpCreateManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WarpCreateCommand implements CommandExecutor {

    private final WarpCreateManager warpManager;
    private final SystemPlugin plugin;
    private final FileConfiguration warpsConfig;

    public WarpCreateCommand(SystemPlugin plugin) {
        this.plugin = plugin;
        this.warpManager = new WarpCreateManager(plugin);
        this.warpsConfig = plugin.getCustomConfig("warps.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser ejecutado por jugadores.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("System.commands.warpcreate")) {
            player.sendMessage(warpsConfig.getString("WARP.NOPERMCREATE", "§cNo tienes permiso para crear warps."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUso: /warpcreate <nombre> <warp/command> <slot>");
            return true;
        }

        String warpName = args[0];
        String type = args[1].toLowerCase();
        int slot;
        try {
            slot = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cEl slot debe ser un número.");
            return true;
        }

        boolean success = warpManager.createWarp(warpName, type, slot, player.getLocation());
        if (!success) {
            sender.sendMessage("§cEl tipo debe ser 'warp' o 'command'.");
            return true;
        }

        sender.sendMessage(MessageUtils.getColoredMessage("&aEl warp '" + warpName + "' ha sido creado correctamente."));
        return true;
    }
}