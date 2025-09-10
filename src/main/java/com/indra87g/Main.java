package com.indra87g;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import cn.nukkit.utils.Config;

import com.indra87g.commands.*;
import com.indra87g.game.MathGameListener;
import com.indra87g.game.MathGameManager;
import com.indra87g.listeners.CooldownListener;
import com.indra87g.listeners.RoamListener;
import com.indra87g.listeners.ServersListener;
import com.indra87g.rewards.DailyRewardManager;
import com.indra87g.rewards.TimeRewardManager;
import com.indra87g.utils.MessageHandler;

import java.io.File;
import java.util.*;

public class Main extends PluginBase {
    private Map<String, Integer> commandCooldowns = new HashMap<>();
    private Set<String> commandHidden = new HashSet<>();
    private Map<String, List<String>> commandAliases = new HashMap<>();

    private DailyRewardManager dailyRewardManager;
    private MathGameManager mathGameManager;

    @Override
    public void onEnable() {
        getLogger().info("§aPlugin activated!");
        if (getServer().getPluginManager().getPlugin("EconomyAPI") == null) {
            getLogger().warning("EconomyAPI not found! Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadConfigs();
        registerCommands();
        registerListeners();

        new TimeRewardManager(this); // This seems to be self-contained
        getLogger().info("All managers enabled!");
    }

    private void loadConfigs() {
        // Load messages first so it can be used by other components.
        MessageHandler.loadMessages(this);

        this.saveResource("commands.yml", false);
        this.saveResource("time_rewards.yml", false);
        this.saveResource("daily_rewards.yml", false);
        this.saveResource("math_game.yml", false); // Save the new config

        File dailyConfig = new File(getDataFolder(), "daily_rewards.yml");
        this.dailyRewardManager = new DailyRewardManager(dailyConfig, getDataFolder());
        this.mathGameManager = new MathGameManager(this);

        File configFile = new File(getDataFolder(), "commands.yml");
        Config config = new Config(configFile, Config.YAML);

        for (String key : config.getKeys(false)) {
            String cmd = key.toLowerCase();
            int cooldown = config.getInt(cmd + ".cooldown", 0);
            boolean hidden = config.getBoolean(cmd + ".hidden", false);
            List<String> aliases = config.getStringList(cmd + ".aliases");

            if (cooldown > 0) {
                commandCooldowns.put(cmd, cooldown);
                if (aliases != null) {
                    for (String alias : aliases) {
                        commandCooldowns.put(alias.toLowerCase(), cooldown);
                    }
                }
            }
            if (hidden) {
                commandHidden.add(cmd);
            }
            if (aliases != null && !aliases.isEmpty()) {
                commandAliases.put(cmd, new ArrayList<>(aliases));
            }
        }
    }

    private void registerCommands() {
        CommandMap map = this.getServer().getCommandMap();
        String pluginName = this.getDescription().getName();

        RoamCommand roamCmd = new RoamCommand(this);
        ServersCommand serversCmd = new ServersCommand(this);

        List<Command> commands = Arrays.asList(
            new SetBlockCommand(),
            new ClearChatCommand(),
            new CasinoCommand(),
            new CalcCommand(mathGameManager), // Pass manager to command
            new DailyCommand(dailyRewardManager),
            roamCmd,
            serversCmd,
            new ReloadCommand()
        );

        for (Command cmd : commands) {
            map.register(pluginName, cmd);

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
                map.register(pluginName, aliasCmd);
            }
        }
        getLogger().info("All commands and aliases registered!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CooldownListener(this, commandCooldowns, commandHidden), this);
        getServer().getPluginManager().registerEvents(new RoamListener(), this);
        getServer().getPluginManager().registerEvents(new ServersListener(), this);
        getServer().getPluginManager().registerEvents(new MathGameListener(mathGameManager), this); // Register new listener
        getLogger().info("All listeners registered!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§aPlugin deactivated!");
    }
}
