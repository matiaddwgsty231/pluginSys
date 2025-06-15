package Sp.System.Commands;

import Sp.System.SystemPlugin;
import Sp.System.Manager.ActionBarManager;
import Sp.System.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor, Listener {

    private final Plugin plugin;
    private final ActionBarManager actionBarManager;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> damageCooldowns = new HashMap<>();
    private final HashMap<UUID, Location> preparationLocations = new HashMap<>();
    private Location spawnLocation;
    private int preparationTime;
    private int damageCooldownTime;
    private int cooldownTime;

    public SpawnCommand(Plugin plugin) {
        this.plugin = plugin;
        this.actionBarManager = new ActionBarManager((SystemPlugin) plugin);
        loadConfiguration();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();

        String worldName = config.getString("spawn.world");
        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");

        if (Bukkit.getWorld(worldName) == null) {
            plugin.getLogger().warning("El mundo '" + worldName + "' no existe. Configura un mundo válido en config.yml.");
            Bukkit.getWorlds().forEach(world -> plugin.getLogger().info("- Mundo disponible: " + world.getName()));
            return;
        }

        spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        plugin.getLogger().info("Spawn configurado en: " + spawnLocation);

        preparationTime = config.getInt("times.preparationTime", 5);
        damageCooldownTime = config.getInt("times.damageCooldownTime", 30);
        cooldownTime = config.getInt("times.cooldownTime", 10);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("prefix");
        String noPerms = plugin.getConfig().getString("Message_Spawn.No_Perms");
        String spawnSetMessage = plugin.getConfig().getString("Message_Spawn.SpawnSet");
        String targetNotOnline = plugin.getConfig().getString("Message_Spawn.SpawnNoOnUser");
        String spawnTimeTag = plugin.getConfig().getString("Message_Spawn.SpawnTimeTag");

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.getColoredMessage(prefix + "&c¡Solo los jugadores pueden usar este comando!"));
                return true;
            }
            Player player = (Player) sender;
            if (isOnDamageCooldown(player)) {
                long timeLeft = (damageCooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                player.sendMessage(MessageUtils.getColoredMessage(prefix + spawnTimeTag.replace("%TimeTag%", String.valueOf(timeLeft))));
                return true;
            }
            handlePlayerSpawn(player, player, false, false);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setspawn")) {
                if (!sender.hasPermission("System.Command.Setspawn")) {
                    sender.sendMessage(MessageUtils.getColoredMessage(prefix + noPerms));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtils.getColoredMessage(prefix + "&c¡Solo los jugadores pueden usar este comando!"));
                    return true;
                }
                Player player = (Player) sender;
                spawnLocation = player.getLocation();
                player.sendMessage(MessageUtils.getColoredMessage(prefix + spawnSetMessage));
                plugin.getLogger().info("El spawn ha sido actualizado a: " + spawnLocation);
                FileConfiguration config = plugin.getConfig();
                config.set("spawn.world", spawnLocation.getWorld().getName());
                config.set("spawn.x", spawnLocation.getX());
                config.set("spawn.y", spawnLocation.getY());
                config.set("spawn.z", spawnLocation.getZ());
                config.set("spawn.yaw", spawnLocation.getYaw());
                config.set("spawn.pitch", spawnLocation.getPitch());
                plugin.saveConfig();
            } else {
                if (!sender.hasPermission("System.Command.SpawnAdmin")) {
                    sender.sendMessage(MessageUtils.getColoredMessage(prefix + noPerms));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(MessageUtils.getColoredMessage(prefix + targetNotOnline));
                    return true;
                }

                handlePlayerSpawn(target, sender instanceof Player ? (Player) sender : null, true, true);
            }
        } else {
            sender.sendMessage(MessageUtils.getColoredMessage(prefix + "&cUso incorrecto del comando."));
            sender.sendMessage(MessageUtils.getColoredMessage("&7 /spawn"));
            if (!sender.hasPermission("System.Command.SpawnAdmin"))
                return true;
            sender.sendMessage(MessageUtils.getColoredMessage("&7 /spawn <jugador>"));
            if (!sender.hasPermission("System.Command.Setspawn"))
                return true;
            sender.sendMessage(MessageUtils.getColoredMessage("&7 /spawn setspawn"));
        }

        return true;
    }

    private void handlePlayerSpawn(Player target, Player executor, boolean isByOther, boolean instant) {
        UUID targetUUID = target.getUniqueId();
        String prefix = plugin.getConfig().getString("prefix");
        String alreadyPreparingMessage = plugin.getConfig().getString("Message_Spawn.SpawnCountdown", "&aQuedan &e%Time% &as para teletransportarte al spawn.");
        alreadyPreparingMessage = alreadyPreparingMessage.replace("%Time%", String.valueOf(preparationTime));

        // Verificar si el jugador ya está en proceso de teletransporte
        if (preparationLocations.containsKey(targetUUID)) {
            target.sendMessage(MessageUtils.getColoredMessage(prefix + alreadyPreparingMessage));
            return;
        }

        String cooldownMessage = plugin.getConfig().getString("Message_Spawn.CooldownSpawn", "&cDebes esperar %CooldownTime% segundos antes de volver a usar el spawn.");
        String spawnTP = plugin.getConfig().getString("Message_Spawn.SpawnTP", "&aHas sido teletransportado al spawn.");
        String spawnCancel = plugin.getConfig().getString("Message_Spawn.SpawnCancel", "&cEl teletransporte ha sido cancelado porque te has movido.");
        String spawnTime = plugin.getConfig().getString("Message_Spawn.SpawnTime", "&aEl teletransporte se realizará en %Time% segundos.");
        cooldowns.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());

        if (!isByOther && cooldowns.containsKey(targetUUID)) {
            long timeLeft = (cooldowns.get(targetUUID) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                target.sendMessage(MessageUtils.getColoredMessage(prefix + cooldownMessage.replace("%CooldownTime%", String.valueOf(timeLeft))));
                return;
            }
        }

        if (instant) {
            target.teleport(spawnLocation);
            target.sendMessage(MessageUtils.getColoredMessage(prefix + spawnTP));
        } else {
            preparationLocations.put(targetUUID, target.getLocation());
            target.sendMessage(MessageUtils.getColoredMessage(prefix + spawnTime.replace("%Time%", String.valueOf(preparationTime))));

            new BukkitRunnable() {
                int timeLeft = preparationTime;

                @Override
                public void run() {
                    if (!preparationLocations.containsKey(targetUUID)) {
                        cancel();
                        return;
                    }

                    Location initialLocation = preparationLocations.get(targetUUID);

                    // Verificar si el jugador se movió
                    if (initialLocation == null || !target.getLocation().toVector().equals(initialLocation.toVector())) {
                        target.sendMessage(MessageUtils.getColoredMessage(prefix + spawnCancel));
                        preparationLocations.remove(targetUUID);
                        cancel();
                        return;
                    }

                    if (timeLeft <= 0) {
                        if (target.isOnline()) {
                            target.teleport(spawnLocation);
                            target.sendMessage(MessageUtils.getColoredMessage(prefix + spawnTP));
                            cooldowns.put(targetUUID, System.currentTimeMillis() + (cooldownTime * 1000));
                        }
                        preparationLocations.remove(targetUUID);
                        cancel();
                        return;
                    }

                    actionBarManager.sendActionBarToPlayer(target, MessageUtils.getColoredMessage(spawnTime.replace("%Time%", String.valueOf(timeLeft))));
                    timeLeft--;
                }
            }.runTaskTimer(plugin, 20L, 20L); // Ejecutar cada 20 ticks (1 segundo)
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String spawnCancel = plugin.getConfig().getString("Message_Spawn.SpawnCancel", "&cEl teletransporte ha sido cancelado porque te has movido.");

        if (preparationLocations.containsKey(playerUUID)) {
            Location initialLocation = preparationLocations.get(playerUUID);
            if (initialLocation != null && !event.getTo().toVector().equals(initialLocation.toVector())) {
                actionBarManager.sendActionBarToPlayer(player, MessageUtils.getColoredMessage(spawnCancel));
                preparationLocations.remove(playerUUID);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            damageCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (damageCooldownTime * 1000));
        }
    }

    private boolean isOnDamageCooldown(Player player) {
        if (!damageCooldowns.containsKey(player.getUniqueId())) {
            return false;
        }

        long timeLeft = damageCooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
        if (timeLeft <= 0) {
            damageCooldowns.remove(player.getUniqueId());
            return false;
        }

        return true;
    }
}
