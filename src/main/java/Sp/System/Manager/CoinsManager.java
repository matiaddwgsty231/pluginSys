package Sp.System.Manager;

import Sp.System.SystemPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class CoinsManager {
    private final File file;
    private final FileConfiguration config;
    private final HashMap<UUID, Integer> playerCoins;

    public CoinsManager() {
        this.file = new File(SystemPlugin.getInstance().getDataFolder(), "coins.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.playerCoins = new HashMap<>();
        loadCoins();
    }

    public void setCoins(UUID playerId, int coins) {
        playerCoins.put(playerId, coins);
        saveCoins(); // Guardar los datos inmediatamente
    }

    public void addCoins(UUID playerId, int amount) {
        int currentCoins = getCoins(playerId);
        setCoins(playerId, currentCoins + amount); // Llama a setCoins, que ya guarda los datos
    }

    public int getCoins(UUID playerId) {
        return playerCoins.getOrDefault(playerId, 0);
    }


    public void saveCoins() {
        // Cargar los datos existentes del archivo
        for (String key : config.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            if (!playerCoins.containsKey(playerId)) {
                playerCoins.put(playerId, config.getInt(key));
            }
        }

        // Guardar todos los datos en el archivo
        for (UUID playerId : playerCoins.keySet()) {
            config.set(playerId.toString(), playerCoins.get(playerId));
        }

        try {
            config.save(file);
            System.out.println("Monedas guardadas correctamente en coins.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadCoins() {
        if (!file.exists()) {
            System.out.println("El archivo coins.yml no existe.");
            return;
        }
        for (String key : config.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            int coins = config.getInt(key);
            playerCoins.put(playerId, coins);
            System.out.println("Cargadas " + coins + " monedas para el jugador " + playerId);
        }
    }

    public String formatCoins(int coins) {
        if (coins >= 1000 && coins < 1000000) {
            return (coins / 1000) + "k";
        } else if (coins >= 1000000) {
            return (coins / 1000000) + "M";
        }
        return String.valueOf(coins);
    }
}