package com.indra87g.listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandCooldownListener implements Listener {

    private final Config cooldownConfig;
    // <playerUUID, <command, lastUsedTimeMillis>>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CommandCooldownListener(PluginBase plugin) {
        plugin.saveResource("cooldowns.yml", false);
        this.cooldownConfig = new Config(plugin.getDataFolder() + "/cooldowns.yml", Config.YAML);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        if (!msg.startsWith("/")) return;

        String[] split = msg.substring(1).split(" ");
        String command = split[0].toLowerCase();

        int cooldown = cooldownConfig.getInt(command, 0);
        if (cooldown <= 0) return; // No cooldown set

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        Map<String, Long> playerCooldowns = cooldowns.getOrDefault(uuid, new HashMap<>());
        long last = playerCooldowns.getOrDefault(command, 0L);
        long elapsed = (now - last) / 1000;

        if (elapsed < cooldown) {
            event.setCancelled(true);
            player.sendMessage("§cPlease wait " + (cooldown - elapsed) + "s before using /" + command + " again.");
        } else {
            playerCooldowns.put(command, now);
            cooldowns.put(uuid, playerCooldowns);
        }
    }
}
