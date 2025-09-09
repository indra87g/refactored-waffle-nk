package com.indra87g.commands

import cn.nukkit.Player
import cn.nukkit.Server
import com.indra87g.utils.MessageHandler

class ReloadCommand : BaseCommand("reload", "Reload a plugin", "/reload <pluginName>", "waffle.reload") {

    override fun validateArgs(args: Array<String>, player: Player): Boolean {
        if (args.size != 1) {
            MessageHandler.sendMessage(player, "reload_usage")
            return false
        }
        return true
    }

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        val pluginName = args[0]
        val plugin = Server.getInstance().pluginManager.getPlugin(pluginName)

        if (plugin == null) {
            MessageHandler.sendMessage(player, "reload_not_found", "{plugin}", pluginName)
            return true
        }

        try {
            Server.getInstance().pluginManager.disablePlugin(plugin)
            Server.getInstance().pluginManager.enablePlugin(plugin)
            MessageHandler.sendMessage(player, "reload_success", "{plugin}", pluginName)
        } catch (e: Exception) {
            MessageHandler.sendMessage(player, "reload_error", "{plugin}", pluginName)
            e.printStackTrace()
        }
        return true
    }
}
