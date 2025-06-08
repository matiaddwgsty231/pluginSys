package Sp.System.Listeners;

import Sp.System.Commands.WarpCommand;
import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WarpListener implements Listener {

    private final SystemPlugin plugin;
    private final WarpCommand warpCommand;

    public WarpListener(SystemPlugin plugin, WarpCommand warpCommand) {
        this.plugin = plugin;
        this.warpCommand = warpCommand;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        if (event.getClickedInventory() == null || !event.getView().getTitle().contains("Warps")) return;

        event.setCancelled(true); // Prevenir mover ítems

        ItemStack item = event.getCurrentItem();
        ItemMeta meta = item.getItemMeta();

        if (meta == null || !(meta instanceof org.bukkit.persistence.PersistentDataHolder)) return;

        PersistentDataContainer container = ((PersistentDataHolder) meta).getPersistentDataContainer();
        if (!container.has(SystemPlugin.getWarpKey(), PersistentDataType.STRING)) return;

        String value = container.get(SystemPlugin.getWarpKey(), PersistentDataType.STRING);

        FileConfiguration warpsConfig = plugin.getCustomConfig("warps.yml");

        if (value.startsWith("menu:")) {
            String targetMenu = value.substring(5);
            warpCommand.onCommand(player, null, "warp", new String[]{targetMenu});
            return;
        }

        // Si está en cooldown
        if (warpCommand.isInCooldown(player)) {
            long timeLeft = warpCommand.getCooldownTimeLeft(player);
            player.sendMessage(MessageUtils.getColoredMessage("&cDebes esperar " + timeLeft + " segundos para usar otro warp."));
            return;
        }

        // Ejecutar warp o comandos
        if (warpsConfig.contains("ITEMS." + value + ".COMMANDS")) {
            warpCommand.executeCommands(player, warpsConfig, value);
        } else {
            warpCommand.teleportPlayer(player, warpsConfig, value);
        }

        warpCommand.applyCooldown(player);
        player.closeInventory();
    }
}
