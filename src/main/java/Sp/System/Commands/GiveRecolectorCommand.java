package Sp.System.Commands;

import Sp.System.Recolector.Recolector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveRecolectorCommand implements CommandExecutor {

    private final Recolector recolector;

    public GiveRecolectorCommand(Recolector recolector) {
        this.recolector = recolector;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /giverecolector <nick> <cantidad>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "El jugador no está conectado.");
            return false;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La cantidad debe ser un número.");
            return false;
        }

        for (int i = 0; i < cantidad; i++) {
            target.getInventory().addItem(recolector.getRecolectorItem());
        }
        sender.sendMessage(ChatColor.GREEN + "Has dado " + cantidad + " recolectores a " + target.getName() + ".");
        return true;
    }
}