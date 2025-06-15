package Sp.System.Commands;

import Sp.System.Manager.ActionBarManager;
import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActionBarCommand implements CommandExecutor {

    private final ActionBarManager actionBarManager;

    public ActionBarCommand(ActionBarManager actionBarManager) {
        this.actionBarManager = actionBarManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("System.Commands.ActionBar")) {
            sender.sendMessage(MessageUtils.getColoredMessage("&cNo tienes permiso para usar este comando."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtils.getColoredMessage("&cUso: /actionbar <all|player> <mensaje>"));
            return true;
        }

        String target = args[0];
        String message = String.join(" ", args).substring(target.length()).trim();

        if (target.equalsIgnoreCase("all")) {
            actionBarManager.sendActionBarToAll(MessageUtils.getColoredMessage(message));
        } else {
            Player player = Bukkit.getPlayer(target);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(MessageUtils.getColoredMessage("&cEl jugador especificado no está en línea."));
                return true;
            }
            actionBarManager.sendActionBarToPlayer(player, MessageUtils.getColoredMessage(message));
        }

        return true;
    }
}