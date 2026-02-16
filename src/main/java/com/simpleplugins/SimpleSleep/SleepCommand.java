package com.simpleplugins.SimpleSleep;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SleepCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_AUTOSKIP = "ssleep.toggle.autoskip";
    private static final String PERMISSION_TOGGLE = "ssleep.toggle";
    private static final String PERMISSION_PHANTOMS = "ssleep.phantoms";
    private static final String PERMISSION_RELOAD = "ssleep.reload";

    private final SimpleSleep plugin;

    public SleepCommand(SimpleSleep plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].equalsIgnoreCase("autoskip") ? "autoskip"
                : args[0].equalsIgnoreCase("toggle") ? "toggle"
                : args[0].equalsIgnoreCase("phantoms") ? "phantoms"
                : args[0].equalsIgnoreCase("reload") ? "reload" : null;

        if (sub == null) {
            sendUsage(sender, label);
            return true;
        }

        switch (sub) {
            case "autoskip" -> {
                if (!sender.hasPermission(PERMISSION_AUTOSKIP)) {
                    sender.sendMessage(withPrefix(plugin.getConfigManager().getNoPermission(PERMISSION_AUTOSKIP)));
                    return true;
                }
                boolean current = plugin.getConfigManager().isAutoSkipNight();
                boolean next = !current;
                plugin.getConfigManager().setAutoSkipNight(next);
                plugin.applySleepState();
                sender.sendMessage(withPrefix(next ? plugin.getConfigManager().getAutoskipEnabled() : plugin.getConfigManager().getAutoskipDisabled()));
                return true;
            }
            case "toggle" -> {
                if (!sender.hasPermission(PERMISSION_TOGGLE)) {
                    sender.sendMessage(withPrefix(plugin.getConfigManager().getNoPermission(PERMISSION_TOGGLE)));
                    return true;
                }
                boolean current = plugin.getConfigManager().isSleepGameruleEnabled();
                boolean next = !current;
                plugin.getConfigManager().setSleepGameruleEnabled(next);
                plugin.applySleepState();
                sender.sendMessage(withPrefix(next ? plugin.getConfigManager().getToggleEnabled() : plugin.getConfigManager().getToggleDisabled()));
                return true;
            }
            case "phantoms" -> {
                if (!sender.hasPermission(PERMISSION_PHANTOMS)) {
                    sender.sendMessage(withPrefix(plugin.getConfigManager().getNoPermission(PERMISSION_PHANTOMS)));
                    return true;
                }
                boolean current = plugin.getConfigManager().isSpawnPhantomsEnabled();
                boolean next = !current;
                plugin.getConfigManager().setSpawnPhantomsEnabled(next);
                plugin.applySleepState();
                sender.sendMessage(withPrefix(next ? plugin.getConfigManager().getPhantomsEnabled() : plugin.getConfigManager().getPhantomsDisabled()));
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission(PERMISSION_RELOAD)) {
                    sender.sendMessage(withPrefix(plugin.getConfigManager().getNoPermission(PERMISSION_RELOAD)));
                    return true;
                }
                plugin.reloadPluginConfig();
                sender.sendMessage(withPrefix(plugin.getConfigManager().getReloadSuccess()));
                return true;
            }
            default -> {
                sendUsage(sender, label);
                return true;
            }
        }
    }

    private String withPrefix(String message) {
        String prefix = plugin.getConfigManager().getPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            return ChatColor.translateAlternateColorCodes('&', prefix + " " + message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(withPrefix(plugin.getConfigManager().getUsage(label)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        String input = args[0].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        if (sender.hasPermission(PERMISSION_AUTOSKIP) && "autoskip".startsWith(input)) {
            suggestions.add("autoskip");
        }
        if (sender.hasPermission(PERMISSION_TOGGLE) && "toggle".startsWith(input)) {
            suggestions.add("toggle");
        }
        if (sender.hasPermission(PERMISSION_PHANTOMS) && "phantoms".startsWith(input)) {
            suggestions.add("phantoms");
        }
        if (sender.hasPermission(PERMISSION_RELOAD) && "reload".startsWith(input)) {
            suggestions.add("reload");
        }
        return suggestions;
    }
}
