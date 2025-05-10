package Sp.System.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import Sp.System.SystemPlugin;

public class ZoneListener implements Listener {

    private final SystemPlugin plugin;
    private boolean zoneEnabled = true;

    public ZoneListener(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void toggleZone() {
        this.zoneEnabled = !this.zoneEnabled;

        if (!zoneEnabled) {

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location playerLocation = player.getLocation();
                if (isInsideZone(playerLocation)) {
                    if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
            }
        }
    }


    public boolean isZoneEnabled() {
        return this.zoneEnabled;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!zoneEnabled) return;

        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (to == null || (to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ())) {
            return;
        }


        if (isInsideZone(to)) {
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(Integer.MAX_VALUE, 0));
            }
        } else {
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }

    private boolean isInsideZone(Location loc) {
        String worldName = plugin.getConfig().getString("zone.world");
        double x1 = plugin.getConfig().getDouble("zone.x1");
        double y1 = plugin.getConfig().getDouble("zone.y1");
        double z1 = plugin.getConfig().getDouble("zone.z1");
        double x2 = plugin.getConfig().getDouble("zone.x2");
        double y2 = plugin.getConfig().getDouble("zone.y2");
        double z2 = plugin.getConfig().getDouble("zone.z2");

        if (loc.getWorld() == null || !loc.getWorld().getName().equals(worldName)) {
            return false;
        }

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
