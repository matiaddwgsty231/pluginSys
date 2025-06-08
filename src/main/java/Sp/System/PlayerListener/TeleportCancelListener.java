package Sp.System.PlayerListener;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import Sp.System.Manager.ActionBarManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeleportCancelListener implements Listener {

    public final Set<UUID> teleportingPlayers = new HashSet<>();
    private final SystemPlugin plugin;

    public TeleportCancelListener(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void addTeleportingPlayer(UUID playerId) {
        teleportingPlayers.add(playerId);
    }

    public void removeTeleportingPlayer(UUID playerId) {
        teleportingPlayers.remove(playerId);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        if (!teleportingPlayers.contains(playerId)) return;

        // Solo cancelar si se mueve de bloque (ignora cambios de cámara)
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

            teleportingPlayers.remove(playerId);

            // Instanciar correctamente ActionBarManager
            ActionBarManager actionBarManager = new ActionBarManager(plugin);

            // Obtener mensaje desde warp.yml y enviarlo al ActionBar
            String cancelMessage = plugin.getCustomConfig("warps.yml").getString("WARP.CANCEL", "&cTeletransporte cancelado porque te moviste.");
            actionBarManager.sendActionBarToPlayer(event.getPlayer(), MessageUtils.getColoredMessage(cancelMessage));
        }
    }
}
