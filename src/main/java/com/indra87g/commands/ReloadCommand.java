package com.indra87g.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("reload", "Reload a plugin", "/reload <pluginName>", new String[]{"waffle.reload"});
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /reload <pluginName>");
            return false;
        }

        String pluginName = args[0];
        Plugin plugin = Server.getInstance().getPluginManager().getPlugin(pluginName);

        if (plugin == null) {
            sender.sendMessage("§cPlugin not found: " + pluginName);
            return true;
        }

        Server.getInstance().getPluginManager().disablePlugin(plugin);
        Server.getInstance().getPluginManager().enablePlugin(plugin);

        sender.sendMessage("§aPlugin " + pluginName + " reloaded!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String typed = args[0].toLowerCase();
            for (Plugin plugin : Server.getInstance().getPluginManager().getPlugins().values()) {
                if (plugin.isEnabled()) {
                    String name = plugin.getName();
                    if (name.toLowerCase().startsWith(typed)) {
                        completions.add(name);
                    }
                }
            }
        }

        return completions;
    }
}
