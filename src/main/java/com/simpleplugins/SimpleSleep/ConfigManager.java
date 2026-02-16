package com.simpleplugins.SimpleSleep;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Loads and provides SimpleSleep configuration values.
 * Supports updating and persisting options from commands.
 */
public class ConfigManager {

    private final SimpleSleep plugin;

    public ConfigManager(SimpleSleep plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads the config from disk and re-reads all values.
     */
    public void reload() {
        plugin.reloadConfig();
    }

    /**
     * When true, night is skipped automatically as soon as it becomes night.
     * When false, night is skipped by the native gamerule playersSleepingPercentage (see getSleepPercentage).
     */
    public boolean isAutoSkipNight() {
        return plugin.getConfig().getBoolean("auto-skip-night", false);
    }

    /**
     * When true, the native gamerule is applied (sleep can skip the night). When false, gamerule is set to 101 (disabled).
     */
    public boolean isSleepGameruleEnabled() {
        return plugin.getConfig().getBoolean("sleep-gamerule-enabled", true);
    }

    /**
     * Whether the native gamerule doInsomnia is enabled (controls phantom spawning).
     * Backed by the config option \"spawn-phantoms\".
     */
    public boolean isSpawnPhantomsEnabled() {
        return plugin.getConfig().getBoolean("spawn-phantoms", true);
    }

    /**
     * Value for the native gamerule "playersSleepingPercentage".
     * 0 or 1 = one player can skip the night; 2-100 = percentage required; 101+ = sleep skip disabled.
     */
    public int getSleepPercentage() {
        return plugin.getConfig().getInt("sleep-percentage", 50);
    }

    /**
     * When true and EssentialsX is present, AFK players are not counted for sleep percentage.
     */
    public boolean isIgnoreAfkPlayers() {
        return plugin.getConfig().getBoolean("ignore-afk-players", true);
    }

    /**
     * Message shown in action bar when players sleep. Placeholders: %sleeping%, %total%. Empty = use vanilla only.
     */
    public String getPlayersSleepingMessage() {
        return plugin.getConfig().getString("messages.players-sleeping", "&e%sleeping%/%total% players sleeping");
    }

    /**
     * Message shown in action bar when the night is skipped natively by sleeping
     * or by AFK-ignore logic. If empty, the vanilla \"Sleeping through this night\" message is used.
     */
    public String getSleepingThroughThisNightMessage() {
        return plugin.getConfig().getString("messages.sleeping-through-this-night", "");
    }

    /**
     * Message shown in action bar when the night is skipped by auto-skip.
     */
    public String getAutoSkipMessage() {
        return plugin.getConfig().getString("messages.auto-skip", "&aSleeping through this night");
    }

    /** Prefix for plugin command messages. */
    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[&aSimpleSleep&7] &r");
    }

    public String getReloadSuccess() {
        return plugin.getConfig().getString("messages.plugin-messages.reload-success", "&aConfiguration reloaded.");
    }

    public String getAutoskipEnabled() {
        return plugin.getConfig().getString("messages.plugin-messages.autoskip-enabled", "&aAuto-skip night is now enabled.");
    }

    public String getAutoskipDisabled() {
        return plugin.getConfig().getString("messages.plugin-messages.autoskip-disabled", "&cAuto-skip night is now disabled.");
    }

    public String getToggleEnabled() {
        return plugin.getConfig().getString("messages.plugin-messages.toggle-enabled", "&aSleep gamerule is now enabled.");
    }

    public String getToggleDisabled() {
        return plugin.getConfig().getString("messages.plugin-messages.toggle-disabled", "&cSleep gamerule is now disabled.");
    }

    public String getPhantomsEnabled() {
        return plugin.getConfig().getString("messages.plugin-messages.phantoms-enabled", "&aInsomnia is now enabled (phantoms can spawn).");
    }

    public String getPhantomsDisabled() {
        return plugin.getConfig().getString("messages.plugin-messages.phantoms-disabled", "&cInsomnia is now disabled (no phantoms will spawn).");
    }

    public String getNoPermission(String permission) {
        return plugin.getConfig().getString("messages.plugin-messages.no-permission", "&cYou do not have permission to run this command. (%permission%)")
                .replace("%permission%", permission);
    }

    /** Placeholder %command% = the command used (e.g. /sleep). */
    public String getUsage(String commandLabel) {
        return plugin.getConfig().getString("messages.plugin-messages.usage", "&7Usage: &f/%command% <autoskip|toggle|phantoms|reload>")
                .replace("%command%", commandLabel);
    }

    /**
     * Sets auto-skip-night and saves the config (persistence).
     */
    public void setAutoSkipNight(boolean value) {
        plugin.getConfig().set("auto-skip-night", value);
        plugin.saveConfig();
    }

    /**
     * Sets sleep-gamerule-enabled and saves the config (persistence).
     */
    public void setSleepGameruleEnabled(boolean value) {
        plugin.getConfig().set("sleep-gamerule-enabled", value);
        plugin.saveConfig();
    }

    /**
     * Sets spawn-phantoms (doInsomnia) and saves the config (persistence).
     */
    public void setSpawnPhantomsEnabled(boolean value) {
        plugin.getConfig().set("spawn-phantoms", value);
        plugin.saveConfig();
    }
}
