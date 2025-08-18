package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import com.indra87g.commands.SetBlockCommand;
import com.indra87g.commands.ClearChatCommand;
import com.indra87g.commands.CasinoCommand;
import com.indra87g.commands.CalcCommand;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;

public class Main extends PluginBase {
    private Map<String, List<String>> aliasesMap = new HashMap<>();
    
    @Override
    public void onEnable() {
        getLogger().info("§aplugin activated!");
        if (getServer().getPluginManager().getPlugin("EconomyAPI") == null) {
            getLogger().warning("EconomyAPI not found! Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.saveResource("aliases.yml", false);
        loadAliases();

        CommandMap map = this.getServer().getCommandMap();

        List<Command> commands = Arrays.asList(
            new SetBlockCommand(),
            new ClearChatCommand(),
            new CasinoCommand(),
            new CalcCommand()
        );

        for (Command cmd : commands) {
            // Register main command
            map.register("waffle", cmd);

            // Register command if any
            List<String> aliases = aliasesMap.getOrDefault(cmd.getName().toLowerCase(), Collections.emptyList());
            for (String alias : aliases) {
                Command aliasCmd = new Command(alias) {
                    @Override
                    public boolean execute(cn.nukkit.command.CommandSender sender, String label, String[] args) {
                        return cmd.execute(sender, label, args);
                    }
                };
                aliasCmd.setDescription("Alias for /" + cmd.getName());
                aliasCmd.setPermission(cmd.getPermission());
                map.register("waffle", aliasCmd);
            }
        }

        getLogger().info("All commands and aliases registered!");
    }

    private void loadAliases() {
        File configFile = new File(getDataFolder(), "aliases.yml");
        if (!configFile.exists()) {
            this.saveResource("aliases.yml", false);
        }
        Config config = new Config(configFile, Config.YAML);
        for (String key : config.getKeys(false)) {
            List<String> aliasList = config.getStringList(key);
            aliasesMap.put(key.toLowerCase(), new ArrayList<>(aliasList));
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("§aplugin deactivated!");
    }
}
