package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import com.indra87g.commands.SetBlockCommand;
import com.indra87g.commands.ClearCommand;
import com.indra87g.commands.CasinoCommand;

public class Main extends PluginBase {

    @Override
    public void onEnable() {
        getLogger().info("§aplugin activated!");
        registerCommands();
    }
    
    private void registerCommands() {
        this.getServer().getCommandMap().register("setblock", new SetBlockCommand());
        this.getServer().getCommandMap().register("clear", new ClearCommand());
        this.getServer().getCommandMap().register("casino", new CasinoCommand());
    }

    if (getServer().getPluginManager().getPlugin("EconomyAPI") == null) {
        getLogger().warning("EconomyAPI not found! Plugin disabled.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }

    
    @Override
    public void onDisable() {
        getLogger().info("§aplugin deactivated!");
    }
}
