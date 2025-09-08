package com.indra87g.listeners;

import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerKickEvent;
import com.indra87g.commands.RoamCommand;

public class RoamListener implements Listener {

    public RoamListener() {
        // Constructor is now empty
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        RoamCommand.getInstance().forceCancel(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        RoamCommand.getInstance().forceCancel(event.getPlayer());
    }
}
