package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.network.protocol.TransferPacket;

import java.io.File;
import java.util.*;

public class ServersCommand extends BaseCommand {

    private final Plugin plugin;
    private final List<ServerEntry> servers = new ArrayList<>();

    public ServersCommand(Plugin plugin) {
        super("servers", "Select target server", "/servers", "waffle.servers");
        this.plugin = plugin;
        loadServers();
    }

    public void loadServers() {
        File file = new File(plugin.getDataFolder(), "servers.yml");
        if (!file.exists()) {
            plugin.saveResource("servers.yml", false);
        }

        Config config = new Config(file, Config.YAML);
        servers.clear();

        if (config.exists("servers")) {
            Map<String, Object> map = config.getSection("servers");
            for (String key : map.keySet()) {
                String name = config.getString("servers." + key + ".name", key);
                String ip = config.getString("servers." + key + ".ip", "127.0.0.1");
                int port = config.getInt("servers." + key + ".port", 19132);
                servers.add(new ServerEntry(name, ip, port));
            }
        }
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (servers.isEmpty()) {
            player.sendMessage("§cNo servers on servers.yml!");
            return true;
        }

        FormWindowSimple form = new FormWindowSimple("§6Server Selector", "Select target server:");
        for (ServerEntry entry : servers) {
            form.addButton(new ElementButton(entry.getName()));
        }
        player.showFormWindow(form, 2025);
        return true;
    }

    public void handleResponse(Player player, int buttonIndex) {
        if (buttonIndex < 0 || buttonIndex >= servers.size()) return;

        ServerEntry entry = servers.get(buttonIndex);

        TransferPacket pk = new TransferPacket();
        pk.address = entry.getIp();
        pk.port = entry.getPort();
        player.dataPacket(pk);

        player.sendMessage("§aConnecting to §e" + entry.getName() +
                           " §7(" + entry.getIp() + ":" + entry.getPort() + ")");
    }

    private static class ServerEntry {
        private final String name;
        private final String ip;
        private final int port;

        public ServerEntry(String name, String ip, int port) {
            this.name = name;
            this.ip = ip;
            this.port = port;
        }

        public String getName() { return name; }
        public String getIp() { return ip; }
        public int getPort() { return port; }
    }
}
