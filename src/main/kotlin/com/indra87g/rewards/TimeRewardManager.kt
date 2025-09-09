package com.indra87g.rewards

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import com.indra87g.listeners.ActivityListener
import me.onebone.economyapi.EconomyAPI
import java.io.File

class TimeRewardManager(private val plugin: PluginBase) {

    private var afkTimeout: Int = 300
    private val lastActivity = mutableMapOf<String, Long>()

    private data class RewardSchedule(
        val interval: Int,
        val reward: Double,
        val message: String,
        val id: String
    )

    init {
        loadConfigAndStart()
        plugin.server.pluginManager.registerEvents(ActivityListener(lastActivity), plugin)
    }

    private fun loadConfigAndStart() {
        val file = File(plugin.dataFolder, "time_rewards.yml")
        if (!file.exists()) {
            plugin.saveResource("time_rewards.yml", false)
        }
        val config = Config(file, Config.YAML)
        afkTimeout = config.getInt("afk_timeout", 300) // default 5 minutes

        val schedules = config.get("schedules") as? List<Map<String, Any>> ?: return

        schedules.forEachIndexed { i, s ->
            val schedule = RewardSchedule(
                interval = s.getOrDefault("interval", 300) as Int,
                reward = s["reward"].toString().toDouble(),
                message = s.getOrDefault("message", "You received ${s["reward"]} money for playing!").toString(),
                id = s.getOrDefault("id", "reward${i + 1}").toString()
            )
            startRewardTask(schedule)
        }
    }

    private fun startRewardTask(sch: RewardSchedule) {
        plugin.server.scheduler.scheduleRepeatingTask(plugin, {
            val now = System.currentTimeMillis()
            for (player in Server.getInstance().onlinePlayers.values) {
                val last = lastActivity.getOrDefault(player.name, now)
                if ((now - last) / 1000 < afkTimeout) {
                    EconomyAPI.getInstance().addMoney(player, sch.reward)
                    player.sendMessage("§a" + sch.message)
                } else {
                    player.sendMessage("§e[AFK] You did not receive the reward (${sch.id}) because you are AFK.")
                }
            }
        }, sch.interval * 20)
    }
}
