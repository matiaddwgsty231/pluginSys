package Sp.System.Commands;

import Sp.System.listeners.ZoneListener;
import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleZoneCommand implements CommandExecutor {

    private final ZoneListener zoneListener;

    public ToggleZoneCommand(ZoneListener zoneListener) {
        this.zoneListener = zoneListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("¡Este comando solo puede ser usado por jugadores!");
            return true;
        }

        zoneListener.toggleZone();

        String status = zoneListener.isZoneEnabled() ? "activada" : "desactivada";
        sender.sendMessage(MessageUtils.getColoredMessage("&7La zona ahora está " + status + "."));

        return true;
    }
}
