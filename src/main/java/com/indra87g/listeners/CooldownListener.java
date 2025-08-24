package com.indra87g.listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerCommandSendEvent;
import cn.nukkit.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CooldownListener implements Listener {

    private final Map<String, Integer> commandCooldowns;
    private final Map<String, Boolean> commandHidden;
    // <playerUUID, <command, lastUsedTimeMillis>>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownListener(Map<String, Integer> commandCooldowns, Map<String, Boolean> commandHidden) {
        this.commandCooldowns = commandCooldowns;
        this.commandHidden = commandHidden;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        if (!msg.startsWith("/")) return;

        String[] split = msg.substring(1).split(" ");
        if (split.length == 0) return;

        String command = split[0].toLowerCase();
        int cooldown = commandCooldowns.getOrDefault(command, 0);
        if (cooldown <= 0) return; // no cooldown set

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

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        Set<String> commands = event.getCommands();
        commands.removeIf(cmd -> commandHidden.getOrDefault(cmd.toLowerCase(), false));
    }
}
