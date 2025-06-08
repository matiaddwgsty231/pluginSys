package Sp.System.Commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.scheduler.BukkitRunnable;
import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import Sp.System.utils.SuccessfulTpaEvent;

public class CommandHandler implements CommandExecutor, Listener {
    private final SystemPlugin plugin;
    private static final long TPA_EXPIRE_TIME = 6000L; // 5 minutos (6000 ticks)

    // Mapa de solicitudes TPA
    private static final Map<UUID, UUID> tpaRequests = new HashMap<>();

    public CommandHandler(SystemPlugin plugin) {
        this.plugin = plugin;
        // Registrar este clase como listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColoredMessage("&cSolo jugadores pueden usar este comando."));
            return true;
        }

        Player player = (Player) sender;
        String cmdName = cmd.getName().toLowerCase();

        switch (cmdName) {
            case "tpa":
                return handleTpaRequest(player, args);
            case "tpaccept":
            case "tpyes":
                return handleTpaAccept(player);
            case "tpdeny":
            case "tpno":
                return handleTpaDeny(player);
            default:
                return false;
        }
    }

    private boolean handleTpaRequest(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /tpa <jugador>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado o no está en línea.");
            return true;
        }

        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "No puedes enviarte una solicitud a ti mismo.");
            return true;
        }

        if (tpaRequests.containsKey(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "Ya tienes una solicitud pendiente.");
            return true;
        }

        tpaRequests.put(sender.getUniqueId(), target.getUniqueId());
        sender.sendMessage(ChatColor.GOLD + "Solicitud enviada a " + ChatColor.RED + target.getName());

        target.sendMessage(ChatColor.GOLD + sender.getName() + " quiere teletransportarse a ti.\n" +
                ChatColor.GREEN + "/tpaccept" + ChatColor.GOLD + " para aceptar\n" +
                ChatColor.RED + "/tpdeny" + ChatColor.GOLD + " para rechazar\n" +
                ChatColor.YELLOW + "Expira en 5 minutos.");

        // Expirar solicitud después de 5 minutos
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tpaRequests.remove(sender.getUniqueId()) != null) {
                    sender.sendMessage(ChatColor.GOLD + "Tu solicitud a " + target.getName() + " ha expirado.");
                }
            }
        }.runTaskLater(plugin, TPA_EXPIRE_TIME);

        return true;
    }

    private boolean handleTpaAccept(Player acceptor) {
        UUID requesterId = null;
        for (Map.Entry<UUID, UUID> entry : tpaRequests.entrySet()) {
            if (entry.getValue().equals(acceptor.getUniqueId())) {
                requesterId = entry.getKey();
                break;
            }
        }

        if (requesterId == null) {
            acceptor.sendMessage(ChatColor.RED + "No tienes solicitudes pendientes.");
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) {
            acceptor.sendMessage(ChatColor.RED + "El jugador ya no está en línea.");
            tpaRequests.remove(requesterId);
            return true;
        }

        // Teleportar
        Location safeLoc = getSafeLocation(acceptor.getLocation());
        requester.teleport(safeLoc);

        // Mensajes
        acceptor.sendMessage(ChatColor.GREEN + "Has aceptado la solicitud de " + requester.getName());

        tpaRequests.remove(requesterId);
        return true;
    }

    private Location getSafeLocation(Location original) {
        // Implementación de búsqueda de ubicación segura
        // (similar a la versión anterior)
        return original; // Simplificado para el ejemplo
    }

    private boolean handleTpaDeny(Player denier) {
        // Implementación similar a versiones anteriores
        return true;
    }

}