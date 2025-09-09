package com.indra87g.commands

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import com.indra87g.utils.MessageHandler

abstract class BaseCommand(name: String, description: String, usage: String, permission: String) :
    Command(name) {

    init {
        this.description = description
        this.usageMessage = usage
        this.permission = permission
    }

    override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
        if (!testPermission(sender)) {
            MessageHandler.sendMessage(sender, "permission_denied")
            return false
        }

        if (sender !is Player) {
            MessageHandler.sendMessage(sender, "player_only_command")
            return false
        }

        if (!validateArgs(args, sender)) {
            // The validateArgs method is responsible for sending the usage message.
            return false
        }

        return try {
            handleCommand(sender, args)
        } catch (e: Exception) {
            MessageHandler.sendMessage(sender, "generic_error")
            e.printStackTrace()
            false
        }
    }

    protected open fun validateArgs(args: Array<String>, player: Player): Boolean {
        return true
    }

    protected abstract fun handleCommand(player: Player, args: Array<String>): Boolean
}
