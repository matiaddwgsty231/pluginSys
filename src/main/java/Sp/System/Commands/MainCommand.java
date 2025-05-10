package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MainCommand implements CommandExecutor {

    private final SystemPlugin plugin;

    public MainCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        String prefix = plugin.getConfig().getString("prefix");
        String noperms = plugin.getConfig().getString("messages_System.No_Perms");
        String reloadMessage = plugin.getConfig().getString("messages_System.reloadMessage");
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("Reload")) {
                subCommandReload(sender, prefix, noperms, reloadMessage);
            } else if (args[0].equalsIgnoreCase("Get")) {
                subCommandsGet(sender, args, prefix, noperms);
            } else {
                help(sender, prefix);
            }
        } else {
            help(sender, prefix);
        }

        return true;
    }
    public void help(CommandSender sender, String prefix) {
        sender.sendMessage(MessageUtils.getColoredMessage("&f&l--------&aComandos de System&f&l--------"));
        sender.sendMessage(MessageUtils.getColoredMessage("&7- /System Reload"));
        sender.sendMessage(MessageUtils.getColoredMessage("&7- /System Get <autor/version>"));
    }
    public void subCommandsGet(CommandSender sender, String[] args, String prefix, String noperms) {
        if (!sender.hasPermission("System.Command.get")) {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + noperms));
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + "&cDebes usar &7/System Get <autor/version>"));
            return;
        }
        if (args[1].equalsIgnoreCase("autor")) {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + "&5El autor del plugin es: &e" + plugin.getDescription().getAuthors()));
        } else if (args[1].equalsIgnoreCase("version")) {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + "&5La versión del plugin es: &f" + plugin.getDescription().getVersion()));
        } else {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + "&cDebes usar &7/System Get <autor/version>"));
        }
    }
    public void subCommandReload(CommandSender sender, String prefix, String noperms, String reloadMessage) {
        if (!sender.hasPermission("System.commands.reload")) {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + noperms));
            return;
        }
        plugin.reloadConfig();
        sender.sendMessage(MessageUtils.getColoredMessage(prefix + reloadMessage));
    }
}
