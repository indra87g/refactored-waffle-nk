package com.indra87g.listeners

import com.indra87g.Main
import cn.nukkit.Player
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerCommandPreprocessEvent
import cn.nukkit.event.server.DataPacketSendEvent
import cn.nukkit.network.protocol.AvailableCommandsPacket
import com.indra87g.utils.MessageHandler
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

class CooldownListener(
    private val plugin: Main,
    private val cooldowns: Map<String, Int>,
    private val hidden: Set<String>
) : Listener {
    private val lastUse = ConcurrentHashMap<String, Long>()

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val msg = e.message
        if (!msg.startsWith("/")) return

        val split = msg.substring(1).split(" ".toRegex()).toTypedArray()
        if (split.isEmpty()) return

        val command = split[0].lowercase()
        val cooldown = cooldowns.getOrDefault(command, 0)
        if (cooldown <= 0) return

        val player = e.player
        if (player.hasPermission("waffle.cooldown.bypass") ||
            player.hasPermission("waffle.cooldown.bypass.$command")
        ) {
            return
        }

        val key = player.uniqueId.toString() + ":" + command
        val now = System.currentTimeMillis()
        val last = lastUse.getOrDefault(key, 0L)
        val elapsed = (now - last) / 1000

        if (elapsed < cooldown) {
            e.isCancelled = true
            val timeRemaining = cooldown - elapsed
            MessageHandler.sendMessage(
                player, "cooldown_active",
                "{time}", timeRemaining.toString(),
                "{command}", command
            )
        } else {
            lastUse[key] = now
        }
    }

    @EventHandler
    fun onPacketSend(e: DataPacketSendEvent) {
        if (e.packet !is AvailableCommandsPacket) return

        val pk = e.packet as AvailableCommandsPacket
        try {
            val f = AvailableCommandsPacket::class.java.getDeclaredField("commands")
            f.isAccessible = true
            val value = f[pk]

            when (value) {
                is MutableMap<*, *> -> {
                    (value.keys as MutableSet<*>).removeIf { k -> hidden.contains(k.toString().lowercase()) }
                }
                is MutableCollection<*> -> {
                    value.removeIf { cd ->
                        try {
                            val nameField: Field = cd!!::class.java.getDeclaredField("name")
                            nameField.isAccessible = true
                            val name = nameField[cd].toString().lowercase()
                            hidden.contains(name)
                        } catch (ignore: Exception) {
                            false
                        }
                    }
                }
            }
        } catch (ex: NoSuchFieldException) {
            plugin.logger.warning("Failed to modify AvailableCommandsPacket: " + ex.message)
        } catch (ex: IllegalAccessException) {
            plugin.logger.warning("Failed to modify AvailableCommandsPacket: " + ex.message)
        }
    }
}
