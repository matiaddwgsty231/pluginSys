package Sp.System.Commands;

import Sp.System.SystemPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadWarpCommand implements CommandExecutor {

    private final SystemPlugin plugin;

    public ReloadWarpCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el sender tiene permiso
        if (!sender.hasPermission("warp.reload")) {
            sender.sendMessage("§cNo tienes permiso para ejecutar este comando.");
            return true;
        }

        // Recargar configuraciones
        plugin.reloadConfig(); // Recarga el archivo de configuración principal
        plugin.reloadCustomConfig("warps.yml"); // Recarga el archivo `warps.yml`

        sender.sendMessage("§aLa configuración de los warps se ha recargado correctamente.");
        return true;
    }
}