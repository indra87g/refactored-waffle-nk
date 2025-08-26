package com.indra87g.listeners;

import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.Player;
import com.indra87g.commands.ServersCommand;

public class ServersListener implements Listener {
    private final ServersCommand serversCommand;

    public ServersListener(ServersCommand serversCommand) {
        this.serversCommand = serversCommand;
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent e) {
        if (!(e.getWindow() instanceof FormWindowSimple)) return;
        if (e.getFormID() != 2025) return;

        Player player = e.getPlayer();
        if (e.getResponse() == null) return; // ditutup tanpa pilih

        FormResponseSimple response = (FormResponseSimple) e.getResponse();
        int buttonIndex = response.getClickedButtonId();

        serversCommand.handleResponse(player, buttonIndex);
    }
}
