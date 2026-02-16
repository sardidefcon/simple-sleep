package com.simpleplugins.SimpleSleep;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

/**
 * Used when "ignore-afk-players" is true and EssentialsX is present.
 * Counts only non-AFK players for sleep percentage and skips the night when the threshold is met.
 */
public class SleepListener implements Listener {

    private static final long MORNING_TIME = 0L;
    /** Uses & for color codes (config-friendly). */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SimpleSleep plugin;

    public SleepListener(SimpleSleep plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        World world = event.getPlayer().getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }
        // Si la opción está desactivada, dejamos que funcione la lógica nativa.
        if (!plugin.getConfigManager().isIgnoreAfkPlayers()) {
            return;
        }

        int total = 0;
        int sleeping = 0;
        boolean anyAfk = false;
        Player entering = event.getPlayer();
        for (Player p : world.getPlayers()) {
            if (!p.isOnline()) {
                continue;
            }
            if (plugin.isAfk(p)) {
                anyAfk = true;
                continue;
            }
            total++;
            if (p.isSleeping() || p.getUniqueId().equals(entering.getUniqueId())) {
                sleeping++;
            }
        }
        // Si no hay jugadores o no hay ningún AFK, dejamos que actúe la función nativa.
        if (total == 0 || !anyAfk) {
            return;
        }
        int pct = plugin.getConfigManager().getSleepPercentage();
        if (pct >= 101) {
            return;
        }
        int required = pct <= 1 ? 1 : (int) Math.ceil(total * pct / 100.0);
        if (sleeping >= required) {
            // Como hay al menos un AFK, aquí sí tomamos el control: cancelamos la acción nativa
            // y aplicamos nuestro propio salto de noche ignorando AFK.
            event.setCancelled(true);

            // Advance to next day's morning (affects full time, not just time-of-day)
            long full = world.getFullTime();
            long dayTime = full % 24000L;
            long nextMorningFull = full - dayTime + 24000L + MORNING_TIME;
            world.setFullTime(nextMorningFull);

            // Reset phantom timer (days since rest) for all players in this world
            for (Player p : world.getPlayers()) {
                if (!p.isOnline()) {
                    continue;
                }
                p.setStatistic(Statistic.TIME_SINCE_REST, 0);
            }

            // Send the same \"sleeping-through-this-night\" style message to all players in the world (AFK-ignore mode)
            String template = plugin.getConfigManager().getSleepingThroughThisNightMessage();
            if (template != null && !template.isEmpty()) {
                Component component = LEGACY.deserialize(template);
                for (Player p : world.getPlayers()) {
                    if (!p.isOnline()) {
                        continue;
                    }
                    p.sendActionBar(component);
                }
            }
        }
    }
}
