package com.indra87g.game;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;

public class MathGameListener implements Listener {

    private final MathGameManager gameManager;

    public MathGameListener(MathGameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isInGame(player)) {
            // Cancel the event to prevent the message from being broadcast
            event.setCancelled(true);
            // Handle the message as a game answer
            gameManager.handleAnswer(player, event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isInGame(player)) {
            // Allow no commands while in the game
            event.setCancelled(true);
        }
    }
}
