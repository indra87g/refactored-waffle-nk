package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import cn.nukkit.utils.Config;

import com.indra87g.commands.SetBlockCommand;
import com.indra87g.commands.ClearChatCommand;
import com.indra87g.commands.CasinoCommand;
import com.indra87g.commands.CalcCommand;
import com.indra87g.commands.DailyCommand;
import com.indra87g.commands.RoamCommand;
import com.indra87g.commands.ServersCommand;
import com.indra87g.commands.ReloadCommand;

import com.indra87g.listeners.RoamListener;
import com.indra87g.listeners.CooldownListener;
import com.indra87g.listeners.ServersListener;
import com.indra87g.listeners.VeinCapitatorListener;

import com.indra87g.rewards.TimeRewardManager;
import com.indra87g.rewards.DailyRewardManager;

import java.io.File;
import java.util.*;

public class Main extends PluginBase {
    private Map<String, Integer> commandCooldowns = new HashMap<>();
    private Set<String> commandHidden = new HashSet<>();
    private Map<String, List<String>> commandAliases = new HashMap<>();

    private TimeRewardManager timeRewardManager;
    private DailyRewardManager dailyRewardManager;
    
    @Override
    public void onEnable() {
        getLogger().info("§aPlugin activated!");
        if (getServer().getPluginManager().getPlugin("EconomyAPI") == null) {
            getLogger().warning("EconomyAPI not found! Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.saveResource("commands.yml", false);
        this.saveResource("time_rewards.yml", false);
        this.saveResource("daily_rewards.yml", false);
        this.dailyRewardManager = new DailyRewardManager(getDataFolder().getPath());

        loadCommandConfig();

        CommandMap map = this.getServer().getCommandMap();
        RoamCommand roamCmd = new RoamCommand(this);
        ServersCommand serversCmd = new ServersCommand(this);

        List<Command> commands = Arrays.asList(
            new SetBlockCommand(),
            new ClearChatCommand(),
            new CasinoCommand(),
            new CalcCommand(),
            new DailyCommand(dailyRewardManager),
            roamCmd,
            serversCmd,
            new ReloadCommand()
        );

        for (Command cmd : commands) {
            map.register("waffle", cmd);

            List<String> aliases = commandAliases.getOrDefault(cmd.getName().toLowerCase(), Collections.emptyList());
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

        getServer().getPluginManager().registerEvents(new CooldownListener(this, commandCooldowns, commandHidden), this);
        getServer().getPluginManager().registerEvents(new RoamListener(roamCmd), this);
        getServer().getPluginManager().registerEvents(new ServersListener(serversCmd), this);
        getServer().getPluginManager().registerEvents(new VeinCapitatorListener(this), this);
        getLogger().info("All commands, aliases, and listeners registered!");

        this.timeRewardManager = new TimeRewardManager(this);
        getLogger().info("TimeRewardManager and DailyRewardManager enabled!");
    }

    private void loadCommandConfig() {
        File configFile = new File(getDataFolder(), "commands.yml");
        if (!configFile.exists()) {
            this.saveResource("commands.yml", false);
        }
        Config config = new Config(configFile, Config.YAML);

        for (String key : config.getKeys(false)) {
            String cmd = key.toLowerCase();
            int cooldown = config.getInt(cmd + ".cooldown", 0);
            boolean hidden = config.getBoolean(cmd + ".hidden", false);
            List<String> aliases = config.getStringList(cmd + ".aliases");

            if (cooldown > 0) commandCooldowns.put(cmd, cooldown);
            if (hidden) commandHidden.add(cmd);
            if (aliases != null && !aliases.isEmpty()) {
                commandAliases.put(cmd, new ArrayList<>(aliases));
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("§aPlugin deactivated!");
    }
}
