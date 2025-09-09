package com.indra87g.commands

import cn.nukkit.Player
import cn.nukkit.plugin.Plugin
import cn.nukkit.scheduler.NukkitRunnable
import com.indra87g.utils.MessageHandler
import me.onebone.economyapi.EconomyAPI
import java.util.*

class RoamCommand(private val plugin: Plugin) :
    BaseCommand("roam", "Enter roaming mode (spectator)", "/roam [cancel|trial]", "waffle.roam") {

    private val roamMinutes = mutableMapOf<UUID, Int>()
    private val roamTasks = mutableMapOf<UUID, NukkitRunnable>()
    private val economyAPI = EconomyAPI.getInstance()

    init {
        instance = this
    }

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            handleRoamStart(player)
            return true
        }

        when (args[0].lowercase()) {
            "cancel" -> handleRoamCancel(player)
            "trial" -> handleRoamTrial(player)
            else -> MessageHandler.sendMessage(player, "roam_usage")
        }
        return true
    }

    private fun handleRoamStart(player: Player) {
        if (roamTasks.containsKey(player.uniqueId)) {
            MessageHandler.sendMessage(player, "roam_already_roaming")
            return
        }

        player.gamemode = Player.SPECTATOR
        MessageHandler.sendMessage(player, "roam_start")
        roamMinutes[player.uniqueId] = 0

        val task = object : NukkitRunnable() {
            override fun run() {
                if (economyAPI.myMoney(player) < 150) {
                    MessageHandler.sendMessage(player, "roam_no_money")
                    cancelRoam(player)
                    return
                }

                economyAPI.reduceMoney(player, 150.0)
                val currentMinutes = roamMinutes.getOrDefault(player.uniqueId, 0) + 1
                roamMinutes[player.uniqueId] = currentMinutes
                MessageHandler.sendMessage(player, "roam_fee_deducted", "{minutes}", currentMinutes.toString())
            }
        }
        task.runTaskTimer(plugin, 20 * 60, 20 * 60)
        roamTasks[player.uniqueId] = task
    }

    private fun handleRoamCancel(player: Player) {
        if (!roamTasks.containsKey(player.uniqueId)) {
            MessageHandler.sendMessage(player, "roam_not_roaming")
            return
        }
        val minutes = roamMinutes.getOrDefault(player.uniqueId, 0)
        cancelRoam(player)
        MessageHandler.sendMessage(player, "roam_ended", "{amount}", (minutes * 150).toString())
    }

    private fun handleRoamTrial(player: Player) {
        player.gamemode = Player.SPECTATOR
        MessageHandler.sendMessage(player, "roam_trial_start")
        plugin.server.scheduler.scheduleDelayedTask(plugin, {
            if (player.isOnline && player.gamemode == Player.SPECTATOR) {
                player.gamemode = Player.SURVIVAL
                MessageHandler.sendMessage(player, "roam_trial_ended")
            }
        }, 20 * 60)
    }

    private fun cancelRoam(player: Player) {
        val id = player.uniqueId
        roamTasks[id]?.cancel()
        roamTasks.remove(id)
        roamMinutes.remove(id)
        if (player.gamemode == Player.SPECTATOR) {
            player.gamemode = Player.SURVIVAL
        }
    }

    fun forceCancel(player: Player) {
        cancelRoam(player)
    }

    companion object {
        private lateinit var instance: RoamCommand
        fun getInstance(): RoamCommand = instance
    }
}
