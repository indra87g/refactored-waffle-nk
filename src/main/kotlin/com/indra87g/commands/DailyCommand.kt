package com.indra87g.commands

import cn.nukkit.Player
import com.indra87g.rewards.DailyRewardManager
import com.indra87g.utils.MessageHandler
import java.time.format.DateTimeFormatter

class DailyCommand(private val rewardManager: DailyRewardManager) :
    BaseCommand("daily", "Daily rewards system", "/daily <claim|status>", "waffle.daily") {

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            MessageHandler.sendMessage(player, "daily_usage")
            return true
        }

        when (args[0].lowercase()) {
            "claim" -> rewardManager.claimReward(player)
            "status" -> sendStatus(player)
            else -> MessageHandler.sendMessage(player, "daily_usage")
        }
        return true
    }

    private fun sendStatus(player: Player) {
        val streak = rewardManager.getStreak(player)
        val now = rewardManager.now
        val today = now.format(DateTimeFormatter.ofPattern("EEEE"))
        val date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        MessageHandler.sendDirectMessage(player, MessageHandler.getMessage("daily_status_header"))
        MessageHandler.sendMessage(player, "daily_status_today", "{day}", today, "{date}", date)
        MessageHandler.sendMessage(player, "daily_status_streak", "{streak}", streak.toString())

        if (rewardManager.hasSpecialReward(date)) {
            MessageHandler.sendMessage(player, "daily_status_special_reward")
        } else {
            MessageHandler.sendMessage(player, "daily_status_no_special_reward")
        }
    }
}
