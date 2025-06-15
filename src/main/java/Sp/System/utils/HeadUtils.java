package Sp.System.utils;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public class HeadUtils {
    private final HeadDatabaseAPI headAPI;
    private final Map<String, ItemStack> headCache;

    public HeadUtils() {
        this.headAPI = new HeadDatabaseAPI();
        this.headCache = new HashMap<>();
    }

    public ItemStack getCustomHead(String headId) {
        return headCache.computeIfAbsent(headId, id -> {
            try {
                ItemStack head = headAPI.getItemHead(id);
                return head != null ? head.clone() : new ItemStack(Material.PLAYER_HEAD);
            } catch (Exception e) {
                return new ItemStack(Material.PLAYER_HEAD);
            }
        });
    }

    public void preloadHead(String headId) {
        getCustomHead(headId); // Automáticamente lo cachea
    }
}