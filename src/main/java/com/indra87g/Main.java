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
        CommandMap map = this.getServer().getCommandMap();

        Command[] commands = new Command[]{
            new SetBlockCommand(),
            new ClearChatCommand(),
            new CasinoCommand(),
            new CalcCommand()
        };
        for (Command cmd : commands) {
            map.register("waffle", cmd); // "waffle" = namespace plugin kamu
        }

        getLogger().info("All commands are successfully registered !");
    }

    @Override
    public void onDisable() {
        getLogger().info("§aplugin deactivated!");
    }
}
