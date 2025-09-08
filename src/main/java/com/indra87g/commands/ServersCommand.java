package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.TransferPacket;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.indra87g.utils.MessageHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServersCommand extends BaseCommand {

    private static ServersCommand instance;
    private final Plugin plugin;
    private final List<ServerEntry> servers = new ArrayList<>();

    public ServersCommand(Plugin plugin) {
        super("servers", "Select a server to join", "/servers", "waffle.servers");
        this.plugin = plugin;
        instance = this;
        loadServers();
    }

    public static ServersCommand getInstance() {
        return instance;
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
            MessageHandler.sendMessage(player, "servers_not_found");
            return true;
        }

        String title = TextFormat.colorize(MessageHandler.getMessage("servers_form_title", ""));
        String content = TextFormat.colorize(MessageHandler.getMessage("servers_form_content", ""));
        FormWindowSimple form = new FormWindowSimple(title, content);
        for (ServerEntry entry : servers) {
            form.addButton(new ElementButton(entry.getName()));
        }
        player.showFormWindow(form, 2025);
        return true;
    }

    public void handleResponse(Player player, int buttonIndex) {
        if (buttonIndex < 0 || buttonIndex >= servers.size()) return;

        ServerEntry entry = servers.get(buttonIndex);
        Vector3 startPos = player.clone().floor();

        new NukkitRunnable() {
            int count = 3;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                if (!player.clone().floor().equals(startPos)) {
                    String title = TextFormat.colorize(MessageHandler.getMessage("servers_teleport_cancelled_title"));
                    String subtitle = TextFormat.colorize(MessageHandler.getMessage("servers_teleport_cancelled_subtitle"));
                    player.sendTitle(title, subtitle, 0, 40, 10);
                    MessageHandler.sendMessage(player, "servers_teleport_cancelled");
                    this.cancel();
                    return;
                }

                if (count > 0) {
                    String title = TextFormat.colorize(MessageHandler.getMessage("servers_teleport_countdown_title"));
                    String subtitle = TextFormat.colorize(MessageHandler.getMessage("servers_teleport_countdown_subtitle", "{count}", String.valueOf(count)));
                    player.sendTitle(title, subtitle, 0, 20, 0);
                    count--;
                } else {
                    TransferPacket pk = new TransferPacket();
                    pk.address = entry.getIp();
                    pk.port = entry.getPort();
                    player.dataPacket(pk);

                    MessageHandler.sendMessage(player, "servers_connecting",
                            "{server}", entry.getName(),
                            "{ip}", entry.getIp(),
                            "{port}", String.valueOf(entry.getPort()));
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
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
