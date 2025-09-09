package com.indra87g.utils

import cn.nukkit.command.CommandSender
import cn.nukkit.utils.Config
import cn.nukkit.utils.TextFormat
import com.indra87g.Main
import java.io.File

object MessageHandler {

    private val messages = mutableMapOf<String, String>()
    private var prefix = ""

    fun loadMessages(plugin: Main) {
        val messagesFile = File(plugin.dataFolder, "messages.yml")
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false)
        }
        val messageConfig = Config(messagesFile, Config.YAML)
        prefix = TextFormat.colorize(messageConfig.getString("prefix", "&7[&eWaffle&7] &r"))

        for (key in messageConfig.keys) {
            if (key != "prefix") {
                messages[key] = messageConfig.getString(key)
            }
        }
    }

    fun sendMessage(sender: CommandSender, key: String, vararg replacements: String) {
        var message = messages.getOrDefault(key, "&cMessage not found: $key")
        message = replacePlaceholders(message, *replacements)
        sender.sendMessage(prefix + TextFormat.colorize(message))
    }

    fun sendDirectMessage(sender: CommandSender, message: String) {
        sender.sendMessage(TextFormat.colorize(message))
    }

    fun getMessage(key: String): String {
        return messages.getOrDefault(key, "&cMessage not found: $key")
    }

    fun getMessage(key: String, vararg replacements: String): String {
        var message = getMessage(key)
        return replacePlaceholders(message, *replacements)
    }

    private fun replacePlaceholders(message: String, vararg replacements: String): String {
        if (replacements.size % 2 != 0) {
            return message // Invalid replacements
        }
        var tempMessage = message
        for (i in replacements.indices step 2) {
            tempMessage = tempMessage.replace(replacements[i], replacements[i + 1])
        }
        return tempMessage
    }
}
