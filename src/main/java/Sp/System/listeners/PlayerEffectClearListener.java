package Sp.System.listeners;

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
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);

            player.setAbsorptionAmount(0.0);
        }, 1L);

    }
}
