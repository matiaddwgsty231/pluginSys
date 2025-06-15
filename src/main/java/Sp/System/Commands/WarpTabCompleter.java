package Sp.System.Commands;

import Sp.System.SystemPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class WarpTabCompleter implements TabCompleter {
    private final SystemPlugin plugin;

    public WarpTabCompleter(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        FileConfiguration warpsConfig = plugin.getCustomConfig("warps.yml");

        // Solo autocompletar si es el primer argumento (para /warp <nombre>)
        if (args.length == 1) {
            if (warpsConfig.contains("ITEMS")) {
                // Obtener todos los warps definidos en ITEMS
                suggestions.addAll(warpsConfig.getConfigurationSection("ITEMS").getKeys(false));
            }
        }

        // Filtrar sugerencias basadas en lo que el jugador está escribiendo
        if (!args[0].isEmpty()) {
            suggestions.removeIf(warp -> !warp.toLowerCase().startsWith(args[0].toLowerCase()));
        }

        return suggestions;
    }
}