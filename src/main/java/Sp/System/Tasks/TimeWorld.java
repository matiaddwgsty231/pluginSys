package Sp.System.Tasks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TimeWorld implements Listener {

    private final JavaPlugin plugin;

    public TimeWorld(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    public void startKeepDayTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                if (world.getTime() >= 13000) {
                    world.setTime(1000);
                }
            }
        }, 0L, 100L);
    }
}