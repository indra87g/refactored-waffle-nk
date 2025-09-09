package com.indra87g.rewards

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.item.Item
import cn.nukkit.potion.Effect
import cn.nukkit.utils.Config
import com.indra87g.utils.MessageHandler
import me.onebone.economyapi.EconomyAPI
import java.io.File
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DailyRewardManager(configFile: File, dataFolder: File) {
    private val mainConfig: Config = Config(configFile, Config.YAML)
    private val dataConfig: Config
    private val zoneId: ZoneId
    private val resetTime: LocalTime

    val now: ZonedDateTime
        get() = ZonedDateTime.now(zoneId)

    init {
        val dataFile = File(dataFolder, "players.yml")
        this.dataConfig = Config(dataFile, Config.YAML)
        this.zoneId = ZoneId.of(mainConfig.getString("timezone", "UTC"))
        this.resetTime = LocalTime.parse(mainConfig.getString("claim_reset", "00:00"))
    }

    fun claimReward(player: Player): Boolean {
        val name = player.name.lowercase()
        val lastClaimSeconds = dataConfig.getLong("$name.last_claim", 0)
        var streak = dataConfig.getInt("$name.streak", 0)
        val now = ZonedDateTime.now(zoneId)
        val lastReset = getResetTime(now)
        val prevReset = lastReset.minusDays(1)

        if (lastClaimSeconds >= lastReset.toEpochSecond()) {
            val nextReset = lastReset.plusDays(1)
            val secondsRemaining = now.until(nextReset, ChronoUnit.SECONDS)
            val hours = secondsRemaining / 3600
            val minutes = secondsRemaining % 3600 / 60
            val timeRemaining = String.format("%dh %dm", hours, minutes)
            MessageHandler.sendMessage(player, "daily_reward_already_claimed", "{time}", timeRemaining)
            return false
        }

        streak = if (lastClaimSeconds >= prevReset.toEpochSecond() && lastClaimSeconds < lastReset.toEpochSecond()) {
            streak + 1
        } else {
            1
        }

        dataConfig.set("$name.last_claim", now.toEpochSecond())
        dataConfig.set("$name.streak", streak)
        dataConfig.save()

        var rewardGiven = false
        val todayDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (mainConfig.exists("special_rewards.$todayDate")) {
            giveRewards(player, mainConfig.getMapList("special_rewards.$todayDate"))
            MessageHandler.sendMessage(player, "daily_reward_special", "{date}", todayDate)
            rewardGiven = true
        }

        val dayOfWeek = now.dayOfWeek.value // Monday=1, Sunday=7
        val rewardKey = "rewards.reward$dayOfWeek"
        if (mainConfig.isList(rewardKey)) {
            giveRewards(player, mainConfig.getMapList(rewardKey))
            val dayName = now.dayOfWeek.toString()
            MessageHandler.sendMessage(player, "daily_reward_success", "{day}", dayName)
            rewardGiven = true
        }

        if (mainConfig.exists("streak_rewards.$streak")) {
            giveRewards(player, mainConfig.getMapList("streak_rewards.$streak"))
            MessageHandler.sendMessage(player, "daily_reward_streak_bonus", "{streak}", streak.toString())
            rewardGiven = true
        }

        if (!rewardGiven) {
            MessageHandler.sendMessage(player, "daily_reward_not_available")
        }
        return rewardGiven
    }

    private fun giveRewards(player: Player, rewards: List<Map<*, *>>) {
        rewards.forEach { reward ->
            when (reward["type"].toString().lowercase()) {
                "item" -> {
                    val id = reward["id"].toString().toInt()
                    val amount = reward["amount"].toString().toInt()
                    player.inventory.addItem(Item(id, 0, amount))
                }
                "money" -> {
                    val money = reward["amount"].toString().toDouble()
                    EconomyAPI.getInstance().addMoney(player, money)
                }
                "effect" -> {
                    val effect = Effect.getEffectByName(reward["effect"].toString().uppercase())
                    if (effect != null) {
                        effect.duration = reward["duration"].toString().toInt() * 20
                        effect.setAmplifier(reward["amplifier"].toString().toInt())
                        player.addEffect(effect)
                    }
                }
                "exp" -> {
                    val exp = reward["amount"].toString().toInt()
                    player.addExperience(exp)
                }
                "command" -> {
                    val cmd = reward["command"].toString().replace("%player%", player.name)
                    Server.getInstance().dispatchCommand(Server.getInstance().consoleSender, cmd)
                }
            }
        }
    }

    private fun getResetTime(now: ZonedDateTime): ZonedDateTime {
        return now.withHour(resetTime.hour).withMinute(resetTime.minute).withSecond(0).withNano(0)
    }

    fun getStreak(player: Player): Int {
        return dataConfig.getInt("${player.name.lowercase()}.streak", 0)
    }

    fun hasSpecialReward(date: String): Boolean {
        return mainConfig.exists("special_rewards.$date")
    }
}
