package Sp.System.PlayerListener;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;

public class PlayerEffectClearListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("SystemPlugin"), () -> {
            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            }

            try {
                player.getClass().getMethod("setAbsorptionAmount", double.class).invoke(player, 0.0);
            } catch (Exception e) {
                // Método no disponible, ignorar o manejar según sea necesario
            }
        }, 1L);
    }
}