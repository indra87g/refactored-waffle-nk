package com.indra87g.commands

import cn.nukkit.Player
import com.indra87g.utils.MessageHandler

class ClearChatCommand :
    BaseCommand("clearchat", "Clear your chat", "/clearchat [amount]", "waffle.clear.chat") {

    override fun validateArgs(args: Array<String>, player: Player): Boolean {
        if (args.size > 1) {
            MessageHandler.sendMessage(player, "clearchat_too_many_args")
            MessageHandler.sendMessage(player, "invalid_usage", "{usage}", usageMessage)
            return false
        }
        return true
    }

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        var amount = 100 // default

        if (args.isNotEmpty()) {
            val parsedAmount = args[0].toIntOrNull()
            if (parsedAmount == null) {
                MessageHandler.sendMessage(player, "clearchat_invalid_amount_type")
                return false
            }
            if (parsedAmount !in 100..500) {
                MessageHandler.sendMessage(player, "clearchat_invalid_amount_range")
                return false
            }
            amount = parsedAmount
        }

        player.sendMessage("\n".repeat(amount))

        MessageHandler.sendMessage(player, "clearchat_success", "{amount}", amount.toString())
        return true
    }
}
