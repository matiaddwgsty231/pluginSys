package Sp.System.listeners;

import org.bukkit.Location;

public class Zona {
    private final String worldName;
    private final double x1, y1, z1, x2, y2, z2;

    public Zona(String worldName, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.worldName = worldName;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean estaDentro(Location location) {
        if (!location.getWorld().getName().equalsIgnoreCase(this.worldName)) {
            return false; // Si el jugador no está en el mismo mundo, no está en la zona.
        }

        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }
}
