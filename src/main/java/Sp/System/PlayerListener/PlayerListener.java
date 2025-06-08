package Sp.System.PlayerListener;

import Sp.System.SystemPlugin;
import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;


import java.util.List;

public class PlayerListener implements Listener {

    private final SystemPlugin plugin;

    public PlayerListener(SystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean welcomeMessageEnabled = plugin.getConfig().getBoolean("Config.welcome_message.enabled", true); // Valor predeterminado: true
        event.setJoinMessage(null);
        if (welcomeMessageEnabled) {
            List<String> welcomeMessages = plugin.getConfig().getStringList("Config.welcome_message.message");

            if (!welcomeMessages.isEmpty()) {
                for (String m : welcomeMessages) {
                    String formattedMessage = m.replace("%player%", player.getName());
                    player.sendMessage(MessageUtils.getColoredMessage(formattedMessage));
                }
            } else {
                player.sendMessage(MessageUtils.getColoredMessage("&7Bienvenido, &f" + player.getName() + "&7!"));
            }
        }

        double x = plugin.getConfig().getDouble("spawn.x");
        double y = plugin.getConfig().getDouble("spawn.y");
        double z = plugin.getConfig().getDouble("spawn.z");
        float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");
        String we = plugin.getConfig().getString("spawn.world");

        if (Bukkit.getWorld(String.valueOf(we)) != null) {
            Location spawnLocation = new Location(Bukkit.getWorld(String.valueOf(we)), x, y, z, yaw, pitch);
            player.teleport(spawnLocation);
        } else {
            Bukkit.getLogger().warning("El mundo %word% no está disponible. El jugador no podrá ser teletransportado.".replace("%word%", String.valueOf(we)));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPressurePlateActivate(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.STONE_PRESSURE_PLATE) {
            return;
        }

        Player player = event.getPlayer();

        //Obtener valores de configuración
        double horizontal = plugin.getConfig().getDouble("push_strength.horizontal", 10.0);
        double vertical = plugin.getConfig().getDouble("push_strength.vertical", 0.05);
        String directionStr = plugin.getConfig().getString("push_direction", "NORTH").toUpperCase();

        //Crear vector de dirección
        Vector direction = new Vector(0, vertical, 0);

        //Asignar dirección horizontal según la configuración
        switch (directionStr) {
            case "NORTH":
                 direction.setZ(-horizontal);
                 break;
            case "SOUTH":
                 direction.setZ(horizontal);
                 break;
            case "EAST":
                 direction.setX(horizontal);
                 break;
            case "WEST":
                 direction.setX(-horizontal);
                 break;
            default:
                // Si la dirección no es válida, usar norte por defecto
                direction.setZ(-horizontal);
                //Mensaje de advertencia
                Bukkit.getLogger().warning("Dirección de empuje no válida: " + directionStr + ". Usando dirección norte por defecto.");
        }
        player.setVelocity(direction);
    }
}
