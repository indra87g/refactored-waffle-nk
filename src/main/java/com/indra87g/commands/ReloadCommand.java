package com.indra87g.commands;

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.Player;
import com.indra87g.utils.MessageHandler;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand() {
        super("reload", "Reload a plugin", "/reload <pluginName>", "waffle.reload");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length != 1) {
            MessageHandler.sendMessage(player, "reload_usage");
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String pluginName = args[0];
        Plugin plugin = Server.getInstance().getPluginManager().getPlugin(pluginName);

        if (plugin == null) {
            MessageHandler.sendMessage(player, "reload_not_found", "{plugin}", pluginName);
            return true;
        }

        try {
            Server.getInstance().getPluginManager().disablePlugin(plugin);
            Server.getInstance().getPluginManager().enablePlugin(plugin);
            MessageHandler.sendMessage(player, "reload_success", "{plugin}", pluginName);
        } catch (Exception e) {
            MessageHandler.sendMessage(player, "reload_error", "{plugin}", pluginName);
            e.printStackTrace();
        }
        return true;
    }
}
