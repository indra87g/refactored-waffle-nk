package com.indra87g.listeners;

import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerKickEvent;
import com.indra87g.commands.RoamCommand;

public class RoamListener implements Listener {
    private final RoamCommand roamCommand;

    public RoamListener(RoamCommand roamCommand) {
        this.roamCommand = roamCommand;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        roamCommand.forceCancel(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        roamCommand.forceCancel(event.getPlayer());
    }
}
