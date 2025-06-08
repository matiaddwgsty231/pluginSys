package Sp.System.Manager;

import Sp.System.SystemPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Collection;

public class CommandManager implements Listener, CommandExecutor {

    private final SystemPlugin plugin;

    public CommandManager(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    // Oculta todos los comandos no configurados
    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();

        // Si tiene permiso para ver todos los comandos, no se oculta nada
        if (player.hasPermission("commandhider.view.all")) return;

        Collection<String> commands = event.getCommands();
        commands.removeIf(cmd -> !plugin.getVisibleCommands().contains(cmd.toLowerCase()));
    }

    // Recarga el config con /hcreload
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("hcreload")) {
            if (!sender.hasPermission("commandhider.reload")) {
                sender.sendMessage("§cNo tienes permiso para usar este comando.");
                return true;
            }

            plugin.reloadConfig();
            plugin.loadVisibleCommands();
            sender.sendMessage("§aConfiguración recargada correctamente.");
            return true;
        }
        return false;
    }
}
