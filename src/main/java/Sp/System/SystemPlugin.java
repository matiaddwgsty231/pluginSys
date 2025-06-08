package Sp.System;

import Sp.System.Commands.*;
import Sp.System.Manager.*;
import Sp.System.PlayerListener.*;
import Sp.System.Tasks.*;
import Sp.System.listeners.*;
import Sp.System.utils.*;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SystemPlugin extends JavaPlugin {

    public static final String prefix = "&f[&c&lSystem&f] ";

    private static SystemPlugin instance;

    private Set<String> visibleCommands;

    private ZoneListener zoneListener;

    private TimeWorld timeWorld;

    private File warpsFile;
    private FileConfiguration warpsConfig;

    private File zonesFile;
    private FileConfiguration zonesConfig;

    private WarpCommand warpCommand;

    private TeleportCancelListener teleportCancelListener;

    //private RecolectorManager recolectorManager;

    //private CoinsManager coinsManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        setupConfigFiles();
        loadVisibleCommands();

        //recolectorManager = new RecolectorManager(getConfig());
        ElytraRestrictionListener listener = new ElytraRestrictionListener();
        //Bukkit.getPluginManager().registerEvents(new PlayerKillListener(coinsManager), this);
        Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        this.teleportCancelListener = new TeleportCancelListener(this);
        getServer().getPluginManager().registerEvents(teleportCancelListener, this);

        this.zoneListener = new ZoneListener(this);
        this.timeWorld = new TimeWorld(this);

        //coinsManager = new CoinsManager();
        //coinsManager.loadCoins();
        timeWorld.startKeepDayTask();

        registerCommands();
        registerEvents();

        //if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
          //  new CoinsPlaceholder(this).register();
        //}

        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                prefix + "&7Ha sido Activado &fVersión " + getDescription().getVersion()
        ));
    }

    @Override
    public void onDisable() {
        //if (coinsManager != null) {
           // coinsManager.saveCoins();
        //}
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                prefix + "&7Ha sido Desactivado"
        ));
        instance = null;
    }

    private void registerCommands() {
        warpCommand = new WarpCommand(this);
        ActionBarManager actionBarManager = new ActionBarManager(this);
        getCommand("system").setExecutor(new MainCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("enderchest").setExecutor(new EnderChestCommand(this));
        getCommand("Tienda").setExecutor(new TiendaCommand(this));
        getCommand("togglezone").setExecutor(new ToggleZoneCommand(zoneListener, this));
        getCommand("tpa").setExecutor(new CommandHandler(this));
        getCommand("tpaccept").setExecutor(new CommandHandler(this));
        getCommand("tpdeny").setExecutor(new CommandHandler(this));
        getCommand("tpyes").setExecutor(new CommandHandler(this));
        getCommand("tpno").setExecutor(new CommandHandler(this));
        getCommand("warpcreate").setExecutor(new WarpCreateCommand(this));
        getCommand("reloadwarp").setExecutor(new ReloadWarpCommand(this));
        getCommand("warp").setExecutor(warpCommand);
        getCommand("pmsg").setExecutor(new PmsgCommand());
        getCommand("actionbar").setExecutor(new ActionBarCommand(actionBarManager));
        getCommand("hcreload").setExecutor(new CommandManager(this));
        //getCommand("givecoins").setExecutor(new GiveCoinsCommand(this));
        //getCommand("Fly").setExecutor(new FlyCommand(this));
        //getCommand("coins").setExecutor(new CoinsCommand(this));
        //getCommand("giverecolector").setExecutor(new GiveRecolectorCommand(recolectorManager.getRecolector()));
    }

    private void registerEvents() {
        WarpCommand warpCommand = new WarpCommand(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(zoneListener, this);
        getServer().getPluginManager().registerEvents(new PlayerEffectClearListener(), this);
        getServer().getPluginManager().registerEvents(timeWorld, this);
        getServer().getPluginManager().registerEvents(new ElytraRestrictionListener(), this);
        getServer().getPluginManager().registerEvents(new WarpListener(this, warpCommand), this);
        getServer().getPluginManager().registerEvents(new CommandManager(this), this);
        //getServer().getPluginManager().registerEvents(recolectorManager.getRecolector(), this);
    }

    private void setupConfigFiles() {
        warpsFile = new File(getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            saveResource("warps.yml", false);
        }
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);

        zonesFile = new File(getDataFolder(), "zones.yml");
        if (!zonesFile.exists()) {
            saveResource("zones.yml", false);
        }
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
    }

    public FileConfiguration getCustomConfig(String fileName) {
        switch (fileName) {
            case "warps.yml":
                return warpsConfig;
            case "zones.yml":
                return zonesConfig;
            default:
                return null;
        }
    }

    public void saveCustomConfig(String fileName) {
        try {
            switch (fileName) {
                case "warps.yml":
                    if (warpsConfig != null) {
                        warpsConfig.save(warpsFile);
                    }
                    break;
                case "zones.yml":
                    if (zonesConfig != null) {
                        zonesConfig.save(zonesFile);
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadCustomConfig(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (file.exists()) {
            if ("warps.yml".equals(fileName)) {
                warpsConfig = YamlConfiguration.loadConfiguration(file);
            } else if ("zones.yml".equals(fileName)) {
                zonesConfig = YamlConfiguration.loadConfiguration(file);
            }
        } else {
            getLogger().warning("El archivo " + fileName + " no existe y no se puede recargar.");
        }
    }

    public Set<String> getVisibleCommands() {
        return visibleCommands;
    }

    public void loadVisibleCommands() {
        visibleCommands = new HashSet<>(getConfig().getStringList("visible-commands"));
    }

    public static NamespacedKey getWarpKey() {
        return new NamespacedKey(getInstance(), "warp_key");
    }

    public static SystemPlugin getInstance() {
        return instance;
    }

    //public CoinsManager getCoinsManager() {
        //return coinsManager;
    //}
    public WarpCommand getWarpCommand() {
        return warpCommand;
    }

    public TeleportCancelListener getTeleportCancelListener() {
        return teleportCancelListener;
    }
}