package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.network.protocol.TransferPacket;

import java.util.HashMap;
import java.util.Map;

public class ServersCommand extends BaseCommand {

    private final Map<String, String[]> servers = new HashMap<>();

    public ServersCommand() {
        super("servers", "Select servers", "/servers", "waffle.servers");

        servers.put("§aLobby", new String[]{"play.example.com", "19132"});
        servers.put("§bSurvival", new String[]{"survival.example.com", "19133"});
        servers.put("§dSkyblock", new String[]{"skyblock.example.com", "19134"});
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        FormWindowSimple form = new FormWindowSimple("§6Server Selector", "Pilih server tujuan:");

        for (String name : servers.keySet()) {
            form.addButton(new ElementButton(name));
        }

        player.showFormWindow(form, 1234);
        return true;
    }
  
    public void handleResponse(Player player, int buttonIndex) {
        if (buttonIndex < 0 || buttonIndex >= servers.size()) return;

        String name = (String) servers.keySet().toArray()[buttonIndex];
        String[] target = servers.get(name);

        String ip = target[0];
        int port = Integer.parseInt(target[1]);

        TransferPacket pk = new TransferPacket();
        pk.address = ip;
        pk.port = port;
        player.dataPacket(pk);

        player.sendMessage("§aConnecting to server §e" + name + " §7(" + ip + ":" + port + ")");
    }
}
