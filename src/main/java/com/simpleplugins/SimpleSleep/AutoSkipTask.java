package com.simpleplugins.SimpleSleep;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Repeating task that skips night when it becomes night, if auto-skip is enabled.
 * Runs once per second.
 */
public class AutoSkipTask extends BukkitRunnable {

    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;
    private static final long MORNING_TIME = 0L;

    /** Uses & for color codes (config-friendly). */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SimpleSleep plugin;

    public AutoSkipTask(SimpleSleep plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().isAutoSkipNight()) {
            return;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }
            long full = world.getFullTime();
            long time = full % 24000L;
            if (time >= NIGHT_START && time < NIGHT_END) {
                // Advance to next day's morning (affects full time, not just time-of-day)
                long nextMorningFull = full - time + 24000L + MORNING_TIME;
                world.setFullTime(nextMorningFull);

                // Reset phantom timer (days since rest) for all players in this world
                for (Player p : world.getPlayers()) {
                    if (!p.isOnline()) {
                        continue;
                    }
                    p.setStatistic(Statistic.TIME_SINCE_REST, 0);
                }

                // Send configurable auto-skip message to all players in this world
                String template = plugin.getConfigManager().getAutoSkipMessage();
                if (template != null && !template.isEmpty()) {
                    Component component = LEGACY.deserialize(template);
                    for (Player p : world.getPlayers()) {
                        if (p.isOnline()) {
                            p.sendActionBar(component);
                        }
                    }
                }
            }
        }
    }
}
