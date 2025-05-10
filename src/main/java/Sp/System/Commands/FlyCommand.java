package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private final SystemPlugin plugin;

    public FlyCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("prefix");
        String noPermsMessage = plugin.getConfig().getString("Message_Fly.No_Perms");
        String errorPlayerNoOn = plugin.getConfig().getString("Message_Fly.Error_Player_No_On");
        String incorrectUsageMessage = plugin.getConfig().getString("Message_Fly.Uso_Incorrecto_Fly");

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.getColoredMessage(MessageUtils.getprefix("&c¡Este comando solo puede ser usado por jugadores!")));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("System.Commands.Fly")) {
                player.sendMessage(MessageUtils.getColoredMessage(prefix + noPermsMessage));
                return true;
            }
            toggleFly(player, player, prefix);
            return true;
        } else if (args.length == 1) {
            if (!sender.hasPermission("System.Commands.FlyOthers")) {
                sender.sendMessage(MessageUtils.getColoredMessage(prefix + noPermsMessage));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(MessageUtils.getColoredMessage(prefix + errorPlayerNoOn.replace("%OtrePlayer%", args[0])));
                return true;
            }
            toggleFly(target, sender, prefix);
            return true;
        } else {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + incorrectUsageMessage));
            return true;
        }
    }

    private void toggleFly(Player target, CommandSender executor, String prefix) {
        String flyOnMessage = plugin.getConfig().getString("Message_Fly.Fly_On");
        String flyOffMessage = plugin.getConfig().getString("Message_Fly.Fly_Off");
        String flyOnOtherMessage = plugin.getConfig().getString("Message_Fly.Fly_On_Otre");
        String flyOffOtherMessage = plugin.getConfig().getString("Message_Fly.Fly_Off_Otre");

        boolean isFlyingAllowed = target.getAllowFlight();
        if (isFlyingAllowed) {
            target.setAllowFlight(false);
            target.sendMessage(MessageUtils.getColoredMessage(prefix + flyOffMessage));
            if (!executor.equals(target)) {
                executor.sendMessage(MessageUtils.getColoredMessage(prefix + flyOffOtherMessage.replace("%OtrePlayer%", target.getName())));
            }
        } else {
            target.setAllowFlight(true);
            target.sendMessage(MessageUtils.getColoredMessage(prefix + flyOnMessage));
            if (!executor.equals(target)) {
                executor.sendMessage(MessageUtils.getColoredMessage(prefix + flyOnOtherMessage.replace("%OtrePlayer%", target.getName())));
            }
        }
    }
}
