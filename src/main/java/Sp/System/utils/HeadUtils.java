package Sp.System.utils;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;

public class HeadUtils {

    private final HeadDatabaseAPI headDatabaseAPI;

    public HeadUtils() {
        this.headDatabaseAPI = new HeadDatabaseAPI();
    }

    public ItemStack getCustomHead(String headID) {
        return headDatabaseAPI.getItemHead(headID); // Obtiene la cabeza personalizada por ID
    }
}