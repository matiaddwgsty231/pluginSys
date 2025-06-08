package Sp.System.Commands;

import Sp.System.listeners.ZoneListener;
import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ToggleZoneCommand implements CommandExecutor {

    private final ZoneListener zoneListener;
    private final Plugin plugin;

    public ToggleZoneCommand(ZoneListener zoneListener, Plugin plugin) {
        this.zoneListener = zoneListener;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("¡Este comando solo puede ser usado por jugadores!");
            return true;
        }
        if (!sender.hasPermission("System.Commands.Togglezone")) {
            sender.sendMessage(MessageUtils.getColoredMessage("&cNo tienes permiso para usar este comando."));
            return true;
        }

        // Alternar el estado de la zona
        zoneListener.toggleZone();

        // Guardar el estado en el config.yml
        plugin.getConfig().set("zone.enabled", zoneListener.isZoneEnabled());
        plugin.saveConfig();

        // Enviar mensaje al jugador
        String status = zoneListener.isZoneEnabled() ? "activada" : "desactivada";
        sender.sendMessage(MessageUtils.getColoredMessage("&7La zona ahora está " + status + "."));

        return true;
    }
}