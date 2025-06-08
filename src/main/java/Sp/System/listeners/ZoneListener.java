package Sp.System.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import Sp.System.SystemPlugin;

public class ZoneListener implements Listener {

    private final SystemPlugin plugin;

    public ZoneListener(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    // Verifica si la zona está habilitada leyendo el config
    public boolean isZoneEnabled() {
        return plugin.getConfig().getBoolean("zone.enabled", true);
    }

    // Alterna el estado y guarda el config
    public void toggleZone() {
        boolean current = isZoneEnabled();
        boolean next = !current;

        plugin.getConfig().set("zone.enabled", next);
        plugin.saveConfig();

        // Si desactivamos la zona, removemos invisibilidad a todos dentro de la zona
        if (!next) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isInsideZone(player.getLocation())) {
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                }
            }
        }
    }

    // Evento que controla invisibilidad al moverse
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        checkAndRemoveInvisibility(player);

        if (!isZoneEnabled()) {
            // Si la zona está desactivada, removemos invisibilidad inmediatamente si la tuviera
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();

        if (to == null || (to.getBlockX() == from.getBlockX() &&
                to.getBlockY() == from.getBlockY() &&
                to.getBlockZ() == from.getBlockZ())) {
            return;
        }

        if (isInsideZone(to)) {
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(Integer.MAX_VALUE, 0));
            }
        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    // Evento que controla invisibilidad al reconectarse
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!isZoneEnabled()) {
            // Zona desactivada => remueve invisibilidad sin importar dónde esté
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            return;
        }

        // Zona activada => aplica invisibilidad si está dentro
        if (isInsideZone(player.getLocation())) {
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(Integer.MAX_VALUE, 0));
            }
        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    public void checkAndRemoveInvisibility(Player player) {
        if (!isZoneEnabled() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }


    // Verifica si la ubicación está dentro de la zona configurada
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

        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }
}
