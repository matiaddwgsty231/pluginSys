package Sp.System.Recolector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.Action;

import java.util.ArrayList;
import java.util.List;

public class Recolector implements Listener {

    private final ItemStack recolectorItem;
    private final boolean autosellHabilitado;
    private final double precioPorItem;
    private Location configuredChest;
    private Location placedRecolector;

    public Recolector(Material material, String nombre, List<String> lore, boolean autosellHabilitado, double precioPorItem) {
        this.recolectorItem = new ItemStack(material);
        ItemMeta meta = recolectorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(nombre);
            List<String> formattedLore = new ArrayList<>();
            for (String line : lore) {
                formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(formattedLore);
            recolectorItem.setItemMeta(meta);
        }
        this.autosellHabilitado = autosellHabilitado;
        this.precioPorItem = precioPorItem;
    }

    public ItemStack getRecolectorItem() {
        return recolectorItem;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().isSimilar(recolectorItem)) {
            // Colocar el recolector si no está colocado
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && placedRecolector == null) {
                if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
                    placedRecolector = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage(ChatColor.GREEN + "¡Recolector colocado correctamente!");
                    event.setCancelled(true);
                }
            }
        } else if (event.getClickedBlock() != null && placedRecolector != null) {
            // Abrir el menú si el jugador interactúa con el recolector colocado
            if (event.getClickedBlock().getLocation().equals(placedRecolector)) {
                event.setCancelled(true);
                openMenu(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (configuredChest != null) {
            Chest chest = (Chest) configuredChest.getBlock().getState();
            Inventory chestInventory = chest.getInventory();

            for (ItemStack drop : event.getBlock().getDrops()) {
                chestInventory.addItem(drop); // Agregar ítems al cofre
            }
            event.setDropItems(false); // Evitar que los ítems caigan al suelo
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Menú del Recolector")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            switch (event.getSlot()) {
                case 0:
                    player.sendMessage(ChatColor.GREEN + "Has mejorado el recolector.");
                    break;
                case 1:
                    player.sendMessage(ChatColor.GREEN + "Has sacado ítems del recolector.");
                    break;
                case 2:
                    player.sendMessage(ChatColor.GREEN + "Haz clic en un cofre para configurarlo.");
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Haz clic derecho en un cofre para configurarlo como almacenamiento.");
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractToConfigureChest(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            // Verificar si el jugador está interactuando con el recolector colocado
            if (placedRecolector != null && event.getClickedBlock().getLocation().equals(placedRecolector)) {
                return; // No configurar el cofre si está interactuando con el recolector
            }

            if (event.getClickedBlock().getType() == Material.CHEST) {
                configuredChest = event.getClickedBlock().getLocation();
                event.getPlayer().sendMessage(ChatColor.GREEN + "¡Cofre configurado correctamente!");
                event.setCancelled(true);
            }
        }
    }

    private void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Menú del Recolector");
        ItemStack mejorar = new ItemStack(Material.ANVIL);
        ItemMeta mejorarMeta = mejorar.getItemMeta();
        if (mejorarMeta != null) {
            mejorarMeta.setDisplayName(ChatColor.GREEN + "Mejorar Recolector");
            mejorar.setItemMeta(mejorarMeta);
        }

        ItemStack sacar = new ItemStack(Material.HOPPER);
        ItemMeta sacarMeta = sacar.getItemMeta();
        if (sacarMeta != null) {
            sacarMeta.setDisplayName(ChatColor.GREEN + "Sacar Ítems");
            sacar.setItemMeta(sacarMeta);
        }

        ItemStack configurar = new ItemStack(Material.CHEST);
        ItemMeta configurarMeta = configurar.getItemMeta();
        if (configurarMeta != null) {
            configurarMeta.setDisplayName(ChatColor.GREEN + "Configurar Cofre");
            configurar.setItemMeta(configurarMeta);
        }

        menu.setItem(0, mejorar);
        menu.setItem(1, sacar);
        menu.setItem(2, configurar);

        player.openInventory(menu);
    }
}