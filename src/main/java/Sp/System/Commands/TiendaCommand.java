package Sp.System.Commands;

import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TiendaCommand implements CommandExecutor {

    private final Plugin plugin;

    public TiendaCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("prefix");
        String Tienda = plugin.getConfig().getString("Message_Store.Tienda");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(MessageUtils.getColoredMessage(prefix + Tienda));
        } else {
            sender.sendMessage(MessageUtils.getprefix("Este comando solo puede ser ejecutado por jugadores."));
        }
        return true;
    }
}
