package Sp.System.Manager;

import Sp.System.Recolector.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class RecolectorManager {

    private final Recolector recolector;

    public RecolectorManager(FileConfiguration config) {
        Material material = Material.valueOf(config.getString("recolector.material", "CHEST"));
        String nombre = ChatColor.translateAlternateColorCodes('&', config.getString("recolector.nombre", "&6Recolector de Chunk"));
        List<String> lore = config.getStringList("recolector.lore");
        boolean autosellHabilitado = config.getBoolean("recolector.autosell.habilitado", true);
        double precioPorItem = config.getDouble("recolector.autosell.precio_por_item", 10.0);

        this.recolector = new Recolector(material, nombre, lore, autosellHabilitado, precioPorItem);
    }

    public Recolector getRecolector() {
        return recolector;
    }
}
