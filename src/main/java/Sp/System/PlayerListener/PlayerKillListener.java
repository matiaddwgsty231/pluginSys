package Sp.System.PlayerListener;

import Sp.System.Manager.CoinsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerKillListener implements Listener {

    private final CoinsManager coinsManager;

    public PlayerKillListener(CoinsManager coinsManager) {
        this.coinsManager = coinsManager;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            UUID killerId = killer.getUniqueId();

            // Incrementar las monedas del jugador
            int newCoins = coinsManager.getCoins(killerId) + 2;
            coinsManager.setCoins(killerId, newCoins);

            // Enviar mensaje al jugador
            killer.sendMessage(ChatColor.GOLD + "Has ganado 2 monedas. Total: " + newCoins + " monedas.");
        }
    }
}