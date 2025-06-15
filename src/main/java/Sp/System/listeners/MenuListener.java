package Sp.System.listeners;

import Sp.System.Manager.MenuManager;
import Sp.System.utils.MessageUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener {
    private final MenuManager menuManager;

    public MenuListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof MenuManager.MenuHolder)) return;

        event.setCancelled(true);
        MenuManager.MenuHolder holder = (MenuManager.MenuHolder) event.getInventory().getHolder();
        MenuManager.MenuItem clickedItem = holder.getMenu().getItem(event.getSlot());

        if (clickedItem != null) {
            clickedItem.getActions().forEach((actionType, actions) -> {
                actions.forEach(action -> {
                    String processed = action.replace("%player%", player.getName());
                    executeAction(player, actionType, processed);
                });
            });
        }
    }

    private void executeAction(Player player, String actionType, String action) {
        switch (actionType.toLowerCase()) {
            case "command":
                player.performCommand(action);
                break;
            case "message":
                player.sendMessage(MessageUtils.getColoredMessage(action));
                break;
            case "sound":
                playSound(player, action);
                break;
            case "open":
                menuManager.openMainMenu(player);
                break;
            case "close":
                player.closeInventory();
                break;
        }
    }

    private void playSound(Player player, String soundData) {
        try {
            String[] parts = soundData.split(":");
            Sound sound = Sound.valueOf(parts[0]);
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof MenuManager.MenuHolder) {
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof MenuManager.MenuHolder) {
        }
    }
}