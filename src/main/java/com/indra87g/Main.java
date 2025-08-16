package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import com.indra87g.commands.SetBlockCommand;
import com.indra87g.commands.ClearChatCommand;
import com.indra87g.commands.CasinoCommand;
import com.indra87g.commands.CalcCommand;

public class Main extends PluginBase {

    @Override
    public void onEnable() {
        getLogger().info("§aplugin activated!");
        if (getServer().getPluginManager().getPlugin("EconomyAPI") == null) {
            getLogger().warning("EconomyAPI not found! Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerCommands();
        CommandMap map = getServer().getCommandMap();
        
        Command existing = map.getCommand("setblock");
        if (existing != null) {
            getLogger().warning("The /setblock command is already registered in another plugin!");
            getLogger().warning("Trying to replace the command with /wsb...");
        }
        
        map.register("wsb", new com.indra87g.commands.SetBlockCommand());
    }
    
    private void registerCommands() {
        this.getServer().getCommandMap().register("setblock", new SetBlockCommand());
        this.getServer().getCommandMap().register("clearchat", new ClearChatCommand());
        this.getServer().getCommandMap().register("casino", new CasinoCommand());
        this.getServer().getCommandMap().register("calc", new CalcCommand());
    }

    @Override
    public void onDisable() {
        getLogger().info("§aplugin deactivated!");
    }
}
