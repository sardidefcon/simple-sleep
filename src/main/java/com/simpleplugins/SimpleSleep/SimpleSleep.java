package com.simpleplugins.SimpleSleep;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;

public class SimpleSleep extends JavaPlugin implements Listener {

    private static final int GAMERULE_DISABLED = 101;

    private ConfigManager configManager;
    private BukkitTask autoSkipTask;
    private Plugin essentialsPlugin;
    private SleepListener sleepListener;
    private boolean afkListenerRegistered;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        this.essentialsPlugin = findEssentialsPlugin();
        this.sleepListener = new SleepListener(this);
        this.afkListenerRegistered = false;

        applySleepState();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new SleepMessageListener(this), this);

        SleepCommand cmd = new SleepCommand(this);
        getCommand("sleep").setExecutor(cmd);
        getCommand("sleep").setTabCompleter(cmd);

        getLogger().info("SimpleSleep has been enabled.");
    }

    @Override
    public void onDisable() {
        stopAutoSkipTask();
        if (afkListenerRegistered) {
            org.bukkit.event.HandlerList.unregisterAll(sleepListener);
            afkListenerRegistered = false;
        }
        getLogger().info("SimpleSleep has been disabled.");
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        applySleepGameruleToWorld(event.getWorld());
        applyInsomniaGameruleToWorld(event.getWorld());
    }

    /**
     * Applies gamerules, auto-skip task, and AFK sleep listener based on current config.
     * Called on enable, reload, and when /sleep autoskip/toggle/phantoms is used.
     */
    public void applySleepState() {
        applySleepGameruleToAllOverworlds();
        applyInsomniaGameruleToAllWorlds();
        startAutoSkipTask();
        updateAfkSleepListener();
    }

    /** Whether we use custom sleep logic (ignoring AFK) instead of the native gamerule. */
    public boolean useAfkIgnoreMode() {
        return configManager.isIgnoreAfkPlayers() && essentialsPlugin != null;
    }

    /**
     * Finds the EssentialsX plugin instance.
     * Note: EssentialsX registers itself under the name "Essentials" in plugin.yml.
     */
    private Plugin findEssentialsPlugin() {
        return getServer().getPluginManager().getPlugin("Essentials");
    }

    private void updateAfkSleepListener() {
        if (useAfkIgnoreMode()) {
            if (!afkListenerRegistered) {
                getServer().getPluginManager().registerEvents(sleepListener, this);
                afkListenerRegistered = true;
            }
        } else {
            if (afkListenerRegistered) {
                org.bukkit.event.HandlerList.unregisterAll(sleepListener);
                afkListenerRegistered = false;
            }
        }
    }

    private void applySleepGameruleToAllOverworlds() {
        for (World world : getServer().getWorlds()) {
            applySleepGameruleToWorld(world);
        }
    }

    private void applySleepGameruleToWorld(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }
        int value = getGameruleValue();
        try {
            world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, value);
        } catch (Exception e) {
            getLogger().warning("Could not set playersSleepingPercentage on world " + world.getName() + ": " + e.getMessage());
        }
    }

    private void applyInsomniaGameruleToAllWorlds() {
        for (World world : getServer().getWorlds()) {
            applyInsomniaGameruleToWorld(world);
        }
    }

    private void applyInsomniaGameruleToWorld(World world) {
        try {
            world.setGameRule(GameRule.DO_INSOMNIA, configManager.isSpawnPhantomsEnabled());
        } catch (Exception e) {
            getLogger().warning("Could not set doInsomnia on world " + world.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Value to set for playersSleepingPercentage: 101 when disabled, else config percentage.
     */
    private int getGameruleValue() {
        if (!configManager.isSleepGameruleEnabled()) {
            return GAMERULE_DISABLED;
        }
        return configManager.getSleepPercentage();
    }

    private void startAutoSkipTask() {
        stopAutoSkipTask();
        if (!configManager.isAutoSkipNight()) {
            return;
        }
        AutoSkipTask task = new AutoSkipTask(this);
        this.autoSkipTask = task.runTaskTimer(this, 20L, 20L);
    }

    private void stopAutoSkipTask() {
        if (autoSkipTask != null) {
            autoSkipTask.cancel();
            autoSkipTask = null;
        }
    }

    /**
     * Returns true if the player is AFK according to EssentialsX (when available and ignore-afk is enabled).
     */
    public boolean isAfk(Player player) {
        if (essentialsPlugin == null || player == null) {
            return false;
        }
        try {
            Method getUser = essentialsPlugin.getClass().getMethod("getUser", Player.class);
            Object user = getUser.invoke(essentialsPlugin, player);
            if (user == null) {
                return false;
            }
            Method isAfk = user.getClass().getMethod("isAfk");
            Object result = isAfk.invoke(user);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        configManager.reload();
        this.essentialsPlugin = findEssentialsPlugin();
        applySleepState();
    }
}
