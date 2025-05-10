package Sp.System.listeners;

import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ElytraRestrictionListener implements Listener {

    private final Set<Zona> zonasRestringidas = new HashSet<>();
    private final Set<Player> jugadoresAvisados = new HashSet<>(); // Para evitar spam de mensajes
    private final String mensajeRestriccion;
    private final String prefijo;

    public ElytraRestrictionListener() {
        // Cargar zonas desde el archivo zones.yml
        cargarZonasDesdeConfig();

        // Cargar el prefijo y mensaje desde la configuración
        prefijo = cargarPrefijoDesdeConfig();
        mensajeRestriccion = cargarMensajeDesdeConfig();
    }

    private void cargarZonasDesdeConfig() {
        // Obtén el archivo zones.yml
        File zonesFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SystemPlugin").getDataFolder(), "zones.yml");
        if (!zonesFile.exists()) {
            // Si el archivo no existe, puedes copiarlo del recurso por defecto
            Bukkit.getServer().getPluginManager().getPlugin("SystemPlugin").saveResource("zones.yml", false);
        }

        // Carga el archivo zones.yml con YamlConfiguration
        YamlConfiguration config = YamlConfiguration.loadConfiguration(zonesFile);
        Set<String> zoneKeys = config.getConfigurationSection("zones").getKeys(false);

        for (String zoneKey : zoneKeys) {
            String worldName = config.getString("zones." + zoneKey + ".world");
            double x1 = config.getDouble("zones." + zoneKey + ".x1");
            double y1 = config.getDouble("zones." + zoneKey + ".y1");
            double z1 = config.getDouble("zones." + zoneKey + ".z1");
            double x2 = config.getDouble("zones." + zoneKey + ".x2");
            double y2 = config.getDouble("zones." + zoneKey + ".y2");
            double z2 = config.getDouble("zones." + zoneKey + ".z2");

            // Crear una nueva zona con los datos cargados
            zonasRestringidas.add(new Zona(worldName, x1, y1, z1, x2, y2, z2));
        }
    }

    private String cargarPrefijoDesdeConfig() {
        // Obtener el prefijo desde el archivo config.yml
        return Bukkit.getPluginManager()
                .getPlugin("SystemPlugin")
                .getConfig()
                .getString("prefix");
    }

    private String cargarMensajeDesdeConfig() {
        // Obtener el mensaje desde el archivo config.yml y añadir el prefijo
        String mensaje = Bukkit.getPluginManager()
                .getPlugin("SystemPlugin")
                .getConfig()
                .getString("messages.elytra_restricted");
        return MessageUtils.getColoredMessage(prefijo + mensaje); // Concatenar el prefijo con el mensaje
    }

    @EventHandler
    public void onPlayerGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            Player jugador = (Player) event.getEntity();

            // Verifica si el jugador ya ha sido avisado
            if (jugadoresAvisados.contains(jugador)) {
                return; // No hacemos nada si ya fue avisado
            }

            // Verifica si el jugador tiene las élitros y está en una zona restringida
            if (jugador.getInventory().getChestplate() != null
                    && jugador.getInventory().getChestplate().getType() == Material.ELYTRA) {
                for (Zona zona : zonasRestringidas) {
                    // Verifica si el jugador está dentro de los límites de la zona
                    if (zona.estaDentro(jugador.getLocation())) {
                        // Cancela el planeo y envía un mensaje solo una vez
                        event.setCancelled(true);
                        jugador.setGliding(false);

                        // Envía el mensaje y agrega al jugador a la lista de avisados
                        jugador.sendMessage(mensajeRestriccion);
                        jugadoresAvisados.add(jugador);

                        // Remueve al jugador de la lista después de 5 segundos
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                jugadoresAvisados.remove(jugador);
                            }
                        }.runTaskLater(Bukkit.getPluginManager().getPlugin("SystemPlugin"), 100L);

                        break; // Ya encontramos una zona restringida, no necesitamos seguir verificando
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player jugador = event.getPlayer();
        ItemStack chestplate = jugador.getInventory().getChestplate();

        // Verificamos si el jugador tiene las élitros equipadas
        if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
            boolean estaEnZona = false;

            for (Zona zona : zonasRestringidas) {
                // Verificar si el jugador está dentro de la zona actual
                if (zona.estaDentro(event.getTo())) {
                    estaEnZona = true;

                    // Si el jugador no ha sido avisado antes, le enviamos el mensaje
                    if (!jugadoresAvisados.contains(jugador)) {
                        jugador.sendMessage(mensajeRestriccion);
                        jugadoresAvisados.add(jugador); // Lo añadimos a la lista de avisados
                    }

                    // Si está planeando, cancelamos el planeo
                    if (jugador.isGliding()) {
                        jugador.setGliding(false);
                    }
                    break; // No necesitamos verificar más zonas
                }
            }

            // Si el jugador ya no está dentro de ninguna zona, lo eliminamos de los avisados
            if (!estaEnZona) {
                jugadoresAvisados.remove(jugador);
            }
        }
    }

}
