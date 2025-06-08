package Sp.System.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PmsgCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("System.Command.Pmsg"))
            return true;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /pmsg <jugador> <mensaje>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "El jugador '" + targetName + "' no está en línea.");
            return true;
        }

        // Construir el mensaje a partir del resto de los argumentos
        String rawMessage = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // Reemplazar la variable %player%
        String senderName = (sender instanceof Player) ? sender.getName() : "Consola";
        rawMessage = rawMessage.replace("%player%", senderName);

        // Traducir colores
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', rawMessage);

        // Enviar solo al jugador destino
        target.sendMessage(coloredMessage);

        return true;
    }
}
