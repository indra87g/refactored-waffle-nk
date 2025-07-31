package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import com.indra87g.commands.SetBlockCommand;
import com.indra87g.commands.ClearCommand;

public class Main extends PluginBase {

    @Override
    public void onEnable() {
        getLogger().info("§aplugin activated!");
        registerCommands();
    }
    
    private void registerCommands() {
        this.getServer().getCommandMap().register("setblock", new SetBlockCommand());
        this.getServer().getCommandMap().register("clear", new ClearCommand());
    }
    
    @Override
    public void onDisable() {
        getLogger().info("§aplugin deactivated!");
    }
}
