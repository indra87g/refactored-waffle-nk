package com.indra87g.listeners;

import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.Player;

import java.util.Map;

public class ActivityListener implements Listener {
    private final Map<String, Long> lastActivity;

    public ActivityListener(Map<String, Long> lastActivity) {
        this.lastActivity = lastActivity;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        lastActivity.put(player.getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        lastActivity.put(player.getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        lastActivity.put(player.getName(), System.currentTimeMillis());
    }
}
