package com.simpleplugins.SimpleSleep;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

/**
 * When the sleep gamerule is active (not in AFK-ignore mode), sends a configurable
 * action bar message to all players in the world when someone enters or leaves a bed.
 * Replaces the vanilla "X/Y players sleeping" message if configured.
 */
public class SleepMessageListener implements Listener {

    /** Uses & for color codes (config-friendly). */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SimpleSleep plugin;

    public SleepMessageListener(SimpleSleep plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        sendSleepMessage(event.getPlayer().getWorld(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        sendSleepMessage(event.getPlayer().getWorld(), null);
    }

    private void sendSleepMessage(World world, Player entering) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        boolean afkIgnoreMode = plugin.useAfkIgnoreMode();
        int total = 0;
        int sleeping = 0;

        for (Player p : world.getPlayers()) {
            if (!p.isOnline()) {
                continue;
            }
            if (afkIgnoreMode && plugin.isAfk(p)) {
                continue;
            }
            total++;
            if (p.isSleeping() || (entering != null && p.getUniqueId().equals(entering.getUniqueId()))) {
                sleeping++;
            }
        }

        // In AFK-ignore mode we only show progress (players-sleeping); SleepListener sends sleeping-through-this-night when skip happens
        int pctOrRule = afkIgnoreMode
                ? plugin.getConfigManager().getSleepPercentage()
                : getRuleValue(world);
        if (pctOrRule < 0 || pctOrRule > 100 || total == 0) {
            pctOrRule = 100;
        }
        int required = pctOrRule <= 1 ? 1 : (int) Math.ceil(total * pctOrRule / 100.0);
        boolean thresholdMet = sleeping >= required;

        String template;
        if (!afkIgnoreMode && thresholdMet) {
            String override = plugin.getConfigManager().getSleepingThroughThisNightMessage();
            template = (override != null && !override.isEmpty())
                    ? override
                    : plugin.getConfigManager().getPlayersSleepingMessage();
        } else {
            template = plugin.getConfigManager().getPlayersSleepingMessage();
        }

        if (template != null && !template.isEmpty()) {
            String message = template
                    .replace("%sleeping%", String.valueOf(sleeping))
                    .replace("%total%", String.valueOf(total));
            Component component = LEGACY.deserialize(message);
            for (Player p : world.getPlayers()) {
                if (!p.isOnline()) continue;
                p.sendActionBar(component);
            }
        }

        if (thresholdMet && !afkIgnoreMode) {
            for (Player p : world.getPlayers()) {
                if (p.isOnline()) {
                    p.setStatistic(org.bukkit.Statistic.TIME_SINCE_REST, 0);
                }
            }
        }
    }

    private int getRuleValue(World world) {
        Integer rule = world.getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE);
        return (rule != null && rule >= 0 && rule <= 100) ? rule : 100;
    }
}
