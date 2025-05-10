package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderChestCommand implements CommandExecutor {

    private final SystemPlugin plugin;

    public EnderChestCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("prefix");
        String No_Perms = plugin.getConfig().getString("Message_EnderChest.No_Perms");
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColoredMessage(
                    MessageUtils.getprefix("Solo los usuarios puede usar este comando.")
            ));
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("System.Commands.EnderChest")) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    prefix + No_Perms
            ));
            return true;
        }
        player.openInventory(player.getEnderChest());
        return true;
    }
}

