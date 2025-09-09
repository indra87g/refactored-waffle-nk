package com.indra87g.listeners

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerChatEvent
import cn.nukkit.event.player.PlayerCommandPreprocessEvent
import cn.nukkit.event.player.PlayerMoveEvent

class ActivityListener(private val lastActivity: MutableMap<String, Long>) : Listener {

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        lastActivity[player.name] = System.currentTimeMillis()
    }

    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        val player = event.player
        lastActivity[player.name] = System.currentTimeMillis()
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        lastActivity[player.name] = System.currentTimeMillis()
    }
}
