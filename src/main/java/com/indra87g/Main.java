package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import com.indra87g.commands.SetBlock;

public class Main extends PluginBase {

    @Override
    public void onEnable() {
        getLogger().info("Plugin activated!");
        registerCommands();
    }
    
    private void registerCommands() {
        this.getServer().getCommandMap().register("setblock", new SetBlock());
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Plugin deactivated!");
    }
}
