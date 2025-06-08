package Sp.System.listeners;

import Sp.System.Commands.WarpCommand;
import Sp.System.SystemPlugin;
import Sp.System.Manager.ActionBarManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class WarpListener implements Listener {

    private final SystemPlugin plugin;
    private final WarpCommand warpCommand;
    private final ActionBarManager actionBarManager;

    public WarpListener(SystemPlugin plugin, WarpCommand warpCommand) {
        this.plugin = plugin;
        this.warpCommand = warpCommand;
        this.actionBarManager = new ActionBarManager(plugin); // Instancia de ActionBarManager
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (event.getView() == null || !event.getView().getTitle().contains("Warps")) return;

        event.setCancelled(true); // Cancelar el click para que no mueva el item

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;
        if (!(meta instanceof PersistentDataHolder)) return; // Validar soporte

        PersistentDataHolder holder = (PersistentDataHolder) meta;
        PersistentDataContainer container = holder.getPersistentDataContainer();

        if (!container.has(SystemPlugin.getWarpKey(), PersistentDataType.STRING)) return;

        String data = container.get(SystemPlugin.getWarpKey(), PersistentDataType.STRING);
        if (data == null) return;

        if (data.startsWith("menu:")) {
            String menu = data.substring(5);
            // Aquí abre el menú usando tu método para abrir inventarios
            plugin.getWarpCommand().openWarpInventory(player, plugin.getCustomConfig("warps.yml"), menu);
            return;
        }

        // Si no es menú, es warp, verificar existencia
        FileConfiguration warpsConfig = plugin.getCustomConfig("warps.yml");
        String path = "ITEMS." + data;
        if (!warpsConfig.contains(path)) {
            player.sendMessage("§cEl warp '" + data + "' no existe.");
            return;
        }

        // Verificar si se requiere permiso
        boolean permissionRequired = warpsConfig.getBoolean(path + ".PERMISSION-REQUIRED", false);
        String permission = warpsConfig.getString(path + ".PERMISSION", "");

        if (permissionRequired && (permission == null || !player.hasPermission(permission))) {
            player.sendMessage("§cNo tienes permiso para usar este warp.");
            return;
        }

        // Verificar cooldown
        if (plugin.getWarpCommand().isInCooldown(player)) {
            player.sendMessage("§cEspera " + plugin.getWarpCommand().getCooldownTimeLeft(player) + " segundos para usar otro warp.");
            return;
        }

        plugin.getWarpCommand().teleportPlayer(player, warpsConfig, data, plugin.getTeleportCancelListener(), actionBarManager); // Se agrega ActionBarManager
        plugin.getWarpCommand().executeCommands(player, warpsConfig, data);
        plugin.getWarpCommand().applyCooldown(player);
    }
}