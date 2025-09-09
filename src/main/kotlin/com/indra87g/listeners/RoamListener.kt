package com.indra87g.listeners

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerKickEvent
import cn.nukkit.event.player.PlayerQuitEvent
import com.indra87g.commands.RoamCommand

class RoamListener : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        RoamCommand.getInstance().forceCancel(event.player)
    }

    @EventHandler
    fun onKick(event: PlayerKickEvent) {
        RoamCommand.getInstance().forceCancel(event.player)
    }
}
