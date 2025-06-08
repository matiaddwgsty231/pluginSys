package Sp.System.Commands;

import Sp.System.SystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCoinsCommand implements CommandExecutor {

    private final SystemPlugin plugin;

    public GiveCoinsCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("system.givecoins")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§cUso: /givecoins <jugador> <cantidad>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cEl jugador especificado no está en línea.");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cLa cantidad debe ser un número válido.");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cLa cantidad debe ser mayor a 0.");
            return true;
        }

        //plugin.getCoinsManager().addCoins(target.getUniqueId(), amount);
        //String formattedAmount = plugin.getCoinsManager().formatCoins(amount);
        //sender.sendMessage("§aHas dado §e" + formattedAmount + " monedas §aal jugador §e" + target.getName() + "§a.");
        //target.sendMessage("§aHas recibido §e" + formattedAmount + " monedas§a.");
        return true;
    }
}