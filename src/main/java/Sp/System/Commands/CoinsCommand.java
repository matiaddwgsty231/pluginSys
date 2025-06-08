package Sp.System.Commands;

import Sp.System.SystemPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinsCommand implements CommandExecutor {

    private final SystemPlugin plugin;

    public CoinsCommand(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        //int coins = plugin.getCoinsManager().getCoins(player.getUniqueId());
       // String formattedCoins = plugin.getCoinsManager().formatCoins(coins); // Formatear las monedas

     //   player.sendMessage("§aTienes §e" + formattedCoins + " monedas§a."); // Mostrar solo el valor formateado
        return true;
    }
}