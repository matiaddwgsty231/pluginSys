package Sp.System.Recolector;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Recolector implements Listener {

    private final ItemStack recolectorItem;
    private final boolean autosellHabilitado;
    private final double precioPorItem;
    private Location placedRecolector;

    public Recolector(Material material, String nombre, List<String> lore, boolean autosellHabilitado, double precioPorItem) {
        this.recolectorItem = new ItemStack(material);
        ItemMeta meta = recolectorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + nombre);
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
        Player player = event.getPlayer();
        ItemStack itemEnMano = event.getItem();

        if (itemEnMano != null) {
            // Mostrar el material del ítem
            player.sendMessage(ChatColor.YELLOW + "Material del ítem en la mano: " + itemEnMano.getType());

            // Mostrar el nombre del ítem
            ItemMeta meta = itemEnMano.getItemMeta();
            if (meta != null) {
                player.sendMessage(ChatColor.YELLOW + "Nombre del ítem en la mano: " + meta.getDisplayName());

                // Mostrar el lore del ítem
                List<String> lore = meta.getLore();
                if (lore != null) {
                    player.sendMessage(ChatColor.YELLOW + "Lore del ítem en la mano:");
                    for (String line : lore) {
                        player.sendMessage(ChatColor.GRAY + "- " + line);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "El ítem en la mano no tiene lore.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "El ítem en la mano no tiene meta.");
            }

            // Comparar con el recolectorItem
            if (itemEnMano.isSimilar(recolectorItem)) {
                player.sendMessage(ChatColor.GREEN + "El ítem en la mano es un recolector.");
            } else {
                player.sendMessage(ChatColor.RED + "El ítem en la mano no es un recolector.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "No tienes ningún ítem en la mano.");
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
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    break;
                case 1:
                    player.sendMessage(ChatColor.GREEN + "Has sacado ítems del recolector.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    break;
                case 2:
                    player.sendMessage(ChatColor.GREEN + "Haz clic en un cofre para configurarlo.");
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Esa opción no es válida.");
                    break;
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
        menu.setItem(0, mejorar);

        ItemStack sacarItems = new ItemStack(Material.CHEST);
        ItemMeta sacarItemsMeta = sacarItems.getItemMeta();
        if (sacarItemsMeta != null) {
            sacarItemsMeta.setDisplayName(ChatColor.YELLOW + "Sacar Ítems");
            sacarItems.setItemMeta(sacarItemsMeta);
        }
        menu.setItem(1, sacarItems);

        ItemStack configurarCofre = new ItemStack(Material.HOPPER);
        ItemMeta configurarCofreMeta = configurarCofre.getItemMeta();
        if (configurarCofreMeta != null) {
            configurarCofreMeta.setDisplayName(ChatColor.AQUA + "Configurar Cofre");
            configurarCofre.setItemMeta(configurarCofreMeta);
        }
        menu.setItem(2, configurarCofre);

        player.openInventory(menu);
    }
}