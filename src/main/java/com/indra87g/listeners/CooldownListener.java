package com.indra87g.listeners;

import com.indra87g.Main;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.AvailableCommandsPacket;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownListener implements Listener {
    private final Main plugin;
    private final Map<String, Integer> cooldowns;
    private final Set<String> hidden;
    private final Map<String, Long> lastUse = new ConcurrentHashMap<>();

    public CooldownListener(Main plugin, Map<String, Integer> cooldowns, Set<String> hidden) {
        this.plugin = plugin;
        this.cooldowns = cooldowns;
        this.hidden = hidden;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (!msg.startsWith("/")) return;

        String[] split = msg.substring(1).split(" ");
        if (split.length == 0) return;

        String command = split[0].toLowerCase();
        int cooldown = cooldowns.getOrDefault(command, 0);
        if (cooldown <= 0) return;

        Player player = e.getPlayer();
        if (player.hasPermission("waffle.cooldown.bypass") ||
            player.hasPermission("waffle.cooldown.bypass." + command)) {
            return;
        }

        String key = player.getUniqueId().toString() + ":" + command;
        long now = System.currentTimeMillis();
        long last = lastUse.getOrDefault(key, 0L);
        long elapsed = (now - last) / 1000;

        if (elapsed < cooldown) {
            e.setCancelled(true);
            player.sendMessage("§cPlease wait " + (cooldown - elapsed) + "s before using /" + command + " again.");
        } else {
            lastUse.put(key, now);
        }
    }

    @EventHandler
    public void onPacketSend(DataPacketSendEvent e) {
        if (!(e.getPacket() instanceof AvailableCommandsPacket)) return;

        AvailableCommandsPacket pk = (AvailableCommandsPacket) e.getPacket();
        try {
            Field f = AvailableCommandsPacket.class.getDeclaredField("commands");
            f.setAccessible(true);
            Object value = f.get(pk);

            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                map.keySet().removeIf(k -> hidden.contains(String.valueOf(k).toLowerCase()));
            } else if (value instanceof Collection) {
                Collection<?> list = (Collection<?>) value;
                list.removeIf(cd -> {
                    try {
                        Field nameField = cd.getClass().getDeclaredField("name");
                        nameField.setAccessible(true);
                        String name = String.valueOf(nameField.get(cd)).toLowerCase();
                        return hidden.contains(name);
                    } catch (Exception ignore) {
                        return false;
                    }
                });
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            plugin.getLogger().warning("Failed to modify AvailableCommandsPacket: " + ex.getMessage());
        }
    }
}
