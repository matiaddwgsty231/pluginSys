package Sp.System.placeholders;

import Sp.System.SystemPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class CoinsPlaceholder extends PlaceholderExpansion {

    private final SystemPlugin plugin;

    public CoinsPlaceholder(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true; // Asegura que el placeholder no se desregistre al recargar.
    }

    @Override
    public boolean canRegister() {
        return true; // Permite registrar el placeholder.
    }

    @Override
    public String getIdentifier() {
        return "system"; // Prefijo del placeholder, por ejemplo: %system_coins%.
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("coins")) {
            int coins = plugin.getCoinsManager().getCoins(player.getUniqueId());
            return plugin.getCoinsManager().formatCoins(coins); // Devuelve el valor formateado
        }
        return null;
    }
}