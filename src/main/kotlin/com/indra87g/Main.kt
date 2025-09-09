package com.indra87g

import cn.nukkit.command.Command
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import com.indra87g.commands.*
import com.indra87g.listeners.CooldownListener
import com.indra87g.listeners.RoamListener
import com.indra87g.listeners.ServersListener
import com.indra87g.rewards.DailyRewardManager
import com.indra87g.rewards.TimeRewardManager
import com.indra87g.utils.MessageHandler
import java.io.File

class Main : PluginBase() {
    private val commandCooldowns = mutableMapOf<String, Int>()
    private val commandHidden = mutableSetOf<String>()
    private val commandAliases = mutableMapOf<String, List<String>>()

    private lateinit var timeRewardManager: TimeRewardManager
    private lateinit var dailyRewardManager: DailyRewardManager

    override fun onEnable() {
        logger.info("§aPlugin activated!")
        if (server.pluginManager.getPlugin("EconomyAPI") == null) {
            logger.warning("EconomyAPI not found! Plugin disabled.")
            server.pluginManager.disablePlugin(this)
            return
        }

        loadConfigs()
        registerCommands()
        registerListeners()

        timeRewardManager = TimeRewardManager(this)
        logger.info("All managers enabled!")
    }

    private fun loadConfigs() {
        MessageHandler.loadMessages(this)
        saveResource("commands.yml", false)
        saveResource("time_rewards.yml", false)
        saveResource("daily_rewards.yml", false)

        val dailyConfig = File(dataFolder, "daily_rewards.yml")
        dailyRewardManager = DailyRewardManager(dailyConfig, dataFolder)

        val configFile = File(dataFolder, "commands.yml")
        if (!configFile.exists()) {
            saveResource("commands.yml", false)
        }
        val config = Config(configFile, Config.YAML)

        for (key in config.keys) {
            val cmd = key.lowercase()
            val cooldown = config.getInt("$cmd.cooldown", 0)
            val hidden = config.getBoolean("$cmd.hidden", false)
            val aliases = config.getStringList("$cmd.aliases")

            if (cooldown > 0) {
                commandCooldowns[cmd] = cooldown
                aliases?.forEach { alias ->
                    commandCooldowns[alias.lowercase()] = cooldown
                }
            }
            if (hidden) {
                commandHidden.add(cmd)
            }
            if (!aliases.isNullOrEmpty()) {
                commandAliases[cmd] = aliases
            }
        }
    }

    private fun registerCommands() {
        val map = server.commandMap
        val pluginName = description.name

        val roamCmd = RoamCommand(this)
        val serversCmd = ServersCommand(this)

        val commands = listOf(
            SetBlockCommand(),
            ClearChatCommand(),
            CasinoCommand(),
            CalcCommand(),
            DailyCommand(dailyRewardManager),
            roamCmd,
            serversCmd,
            ReloadCommand()
        )

        commands.forEach { cmd ->
            map.register(pluginName, cmd)
            commandAliases[cmd.name.lowercase()]?.forEach { alias ->
                val aliasCmd = object : Command(alias) {
                    override fun execute(sender: cn.nukkit.command.CommandSender, label: String, args: Array<String>): Boolean {
                        return cmd.execute(sender, label, args)
                    }
                }
                aliasCmd.description = "Alias for /${cmd.name}"
                aliasCmd.permission = cmd.permission
                map.register(pluginName, aliasCmd)
            }
        }
        logger.info("All commands and aliases registered!")
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(CooldownListener(this, commandCooldowns, commandHidden), this)
        server.pluginManager.registerEvents(RoamListener(), this)
        server.pluginManager.registerEvents(ServersListener(), this)
        logger.info("All listeners registered!")
    }

    override fun onDisable() {
        logger.info("§aPlugin deactivated!")
    }
}
