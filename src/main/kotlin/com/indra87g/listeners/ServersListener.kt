package com.indra87g.listeners

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerFormRespondedEvent
import cn.nukkit.form.response.FormResponseSimple
import cn.nukkit.form.window.FormWindowSimple
import com.indra87g.commands.ServersCommand

class ServersListener : Listener {

    @EventHandler
    fun onFormResponse(e: PlayerFormRespondedEvent) {
        if (e.window !is FormWindowSimple) return
        if (e.formID != 2025) return

        val player = e.player
        if (e.response == null) return // Closed without selection

        val response = e.response as FormResponseSimple
        val buttonIndex = response.clickedButtonId

        ServersCommand.getInstance().handleResponse(player, buttonIndex)
    }
}
