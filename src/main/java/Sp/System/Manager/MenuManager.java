package Sp.System.Manager;

import Sp.System.utils.HeadUtils;
import Sp.System.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MenuManager {
    private final JavaPlugin plugin;
    private final HeadUtils headUtils;
    private final NamespacedKey menuActionKey;
    private CustomMenu mainMenu;
    private boolean texturesLoaded = false;

    public MenuManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.headUtils = new HeadUtils();
        this.menuActionKey = new NamespacedKey(plugin, "menu-action");
        setupMenusFolder();
        preloadTextures();
    }

    private void setupMenusFolder() {
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists() && menusFolder.mkdirs()) {
            createSampleMenu();
        }
    }

    private void createSampleMenu() {
        File sampleFile = new File(plugin.getDataFolder(), "menus/menu.yml");
        if (!sampleFile.exists()) {
            try {
                YamlConfiguration config = new YamlConfiguration();

                ConfigurationSection mainSection = config.createSection("menus.main");
                mainSection.set("title", "&6&lMenú Principal");
                mainSection.set("size", 27);

                // Item seguro inicial
                ConfigurationSection safeItem = mainSection.createSection("items.inicio");
                safeItem.set("slot", 10);
                safeItem.set("material", "CHEST");
                safeItem.set("name", "&aMenú Principal");
                safeItem.set("lore", Arrays.asList("&7Cargando recursos..."));

                config.save(sampleFile);
                plugin.getLogger().info("Archivo menu.yml creado automáticamente");
            } catch (IOException e) {
                plugin.getLogger().warning("Error al crear menú de ejemplo: " + e.getMessage());
            }
        }
    }

    private void preloadTextures() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // 1. Cargar estructura del menú
                    loadMainMenu();

                    // 2. Precargar texturas HD
                    if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
                        plugin.getLogger().info("Precargando texturas HD...");
                        preloadHeads();
                    }

                    texturesLoaded = true;
                    plugin.getLogger().info("Menús precargados correctamente");
                } catch (Exception e) {
                    plugin.getLogger().warning("Error al precargar menús: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void preloadHeads() {
        File menuFile = new File(plugin.getDataFolder(), "menus/menu.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(menuFile);

        if (config.contains("menus.main.items")) {
            config.getConfigurationSection("menus.main.items").getKeys(false).forEach(itemKey -> {
                String material = config.getString("menus.main.items." + itemKey + ".material", "");
                if (material.startsWith("HD-")) {
                    headUtils.preloadHead(material.substring(3));
                }
            });
        }
    }

    public void loadMainMenu() {
        File menuFile = new File(plugin.getDataFolder(), "menus/menu.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(menuFile);
        ConfigurationSection mainMenuSection = config.getConfigurationSection("menus.main");

        if (mainMenuSection != null) {
            this.mainMenu = new CustomMenu(mainMenuSection, headUtils, menuActionKey);
        }
    }

    public void openMainMenu(Player player) {
        if (!texturesLoaded) {
            player.sendMessage(MessageUtils.getColoredMessage("&ePrecargando menú... Por favor espera"));
            new BukkitRunnable() {
                int attempts = 0;

                @Override
                public void run() {
                    if (texturesLoaded) {
                        openMainMenu(player);
                        cancel();
                    } else if (attempts++ > 20) { // Timeout después de 10 segundos
                        player.sendMessage(MessageUtils.getColoredMessage("&cError al cargar el menú"));
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 10L, 10L);
            return;
        }

        if (mainMenu != null) {
            mainMenu.open(player);
        } else {
            player.sendMessage(MessageUtils.getColoredMessage("&cEl menú no está disponible"));
        }
    }

    public static class CustomMenu {
        private final String title;
        private final int size;
        private final Map<Integer, MenuItem> items;

        public CustomMenu(ConfigurationSection config, HeadUtils headUtils, NamespacedKey actionKey) {
            this.title = MessageUtils.getColoredMessage(config.getString("title", "Menú"));
            this.size = config.getInt("size", 27);
            this.items = new HashMap<>();

            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String itemKey : itemsSection.getKeys(false)) {
                    ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                    if (itemConfig != null) {
                        MenuItem item = new MenuItem(itemConfig, headUtils, actionKey);
                        items.put(item.getSlot(), item);
                    }
                }
            }
        }

        public void open(Player player) {
            Inventory inv = Bukkit.createInventory(new MenuHolder(this), size, title);
            items.forEach((slot, item) -> inv.setItem(slot, item.getItemStack()));
            player.openInventory(inv);
        }

        public MenuItem getItem(int slot) {
            return items.get(slot);
        }
    }

    public static class MenuItem {
        private final ItemStack itemStack;
        private final int slot;
        private final Map<String, List<String>> actions;

        public MenuItem(ConfigurationSection config, HeadUtils headUtils, NamespacedKey actionKey) {
            this.slot = config.getInt("slot", 0);
            this.itemStack = createItemStack(config, headUtils);
            this.actions = loadActions(config);

            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(actionKey, PersistentDataType.STRING, "menu_item");
                itemStack.setItemMeta(meta);
            }
        }

        private ItemStack createItemStack(ConfigurationSection config, HeadUtils headUtils) {
            String material = config.getString("material", "STONE");
            ItemStack item;

            if (material.startsWith("HD-")) {
                item = headUtils.getCustomHead(material.substring(3));
                if (item == null || item.getType() == Material.AIR) {
                    item = new ItemStack(Material.matchMaterial(config.getString("fallback", "CHEST")));
                }
            } else {
                Material mat = Material.matchMaterial(material);
                item = new ItemStack(mat != null ? mat : Material.BARRIER);
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessageUtils.getColoredMessage(config.getString("name", "Item")));

                List<String> lore = config.getStringList("lore");
                if (lore != null && !lore.isEmpty()) {
                    meta.setLore(MessageUtils.getColoredMessages(lore));
                }

                item.setItemMeta(meta);
            }

            return item;
        }

        private Map<String, List<String>> loadActions(ConfigurationSection config) {
            Map<String, List<String>> actions = new HashMap<>();

            if (config.contains("actions")) {
                ConfigurationSection actionsSection = config.getConfigurationSection("actions");
                for (String actionType : actionsSection.getKeys(false)) {
                    if (actionsSection.isList(actionType)) {
                        actions.put(actionType, actionsSection.getStringList(actionType));
                    } else {
                        actions.put(actionType, Collections.singletonList(actionsSection.getString(actionType)));
                    }
                }
            }

            return actions;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getSlot() {
            return slot;
        }

        public Map<String, List<String>> getActions() {
            return actions;
        }
    }

    public static class MenuHolder implements InventoryHolder {
        private final CustomMenu menu;

        public MenuHolder(CustomMenu menu) {
            this.menu = menu;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        public CustomMenu getMenu() {
            return menu;
        }
    }
}