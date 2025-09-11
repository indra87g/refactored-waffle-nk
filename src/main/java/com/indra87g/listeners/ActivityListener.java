package com.indra87g.listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import cn.nukkit.Player;

import java.util.HashMap;
import java.util.Map;

public class ActivityListener implements Listener {
    private final Map<String, Long> lastActivity;
    private final Map<String, Map<String, Long>> playerRewardHistory;

    public ActivityListener(Map<String, Long> lastActivity, Map<String, Map<String, Long>> playerRewardHistory) {
        this.lastActivity = lastActivity;
        this.playerRewardHistory = playerRewardHistory;
    }

    private void updateActivity(Player player) {
        lastActivity.put(player.getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateActivity(player);
        // Initialize reward history for the player, setting their start time
        long now = System.currentTimeMillis();
        Map<String, Long> history = playerRewardHistory.computeIfAbsent(player.getName(), k -> new HashMap<>());
        // This ensures they don't get rewards immediately, their cooldown starts now.
        // We will set the last reward time to now for all schedules.
        // This logic is handled in the TimeRewardManager, this is just for initialization.
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up maps to prevent memory leaks
        lastActivity.remove(player.getName());
        playerRewardHistory.remove(player.getName());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Only update on significant movement to avoid spamming map updates
        if (event.getFrom().distance(event.getTo()) > 0.1) {
            updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }
}
