package com.indra87g.commands

import cn.nukkit.Player
import cn.nukkit.form.element.ElementButton
import cn.nukkit.form.window.FormWindowSimple
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.TransferPacket
import cn.nukkit.plugin.Plugin
import cn.nukkit.scheduler.NukkitRunnable
import cn.nukkit.utils.Config
import cn.nukkit.utils.TextFormat
import com.indra87g.utils.MessageHandler
import java.io.File

class ServersCommand(private val plugin: Plugin) :
    BaseCommand("servers", "Select a server to join", "/servers", "waffle.servers") {

    private val servers = mutableListOf<ServerEntry>()

    init {
        instance = this
        loadServers()
    }

    fun loadServers() {
        val file = File(plugin.dataFolder, "servers.yml")
        if (!file.exists()) {
            plugin.saveResource("servers.yml", false)
        }
        val config = Config(file, Config.YAML)
        servers.clear()

        if (config.exists("servers")) {
            val serversMap = config.getSection("servers")
            for (key in serversMap.keys) {
                val name = config.getString("servers.$key.name", key)
                val ip = config.getString("servers.$key.ip", "127.0.0.1")
                val port = config.getInt("servers.$key.port", 19132)
                servers.add(ServerEntry(name, ip, port))
            }
        }
    }

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        if (servers.isEmpty()) {
            MessageHandler.sendMessage(player, "servers_not_found")
            return true
        }

        val title = TextFormat.colorize(MessageHandler.getMessage("servers_form_title", ""))
        val content = TextFormat.colorize(MessageHandler.getMessage("servers_form_content", ""))
        val form = FormWindowSimple(title, content)
        servers.forEach { form.addButton(ElementButton(it.name)) }
        player.showFormWindow(form, 2025)
        return true
    }

    fun handleResponse(player: Player, buttonIndex: Int) {
        if (buttonIndex !in servers.indices) return

        val entry = servers[buttonIndex]
        val startPos = player.floor()

        object : NukkitRunnable() {
            var count = 3
            override fun run() {
                if (!player.isOnline) {
                    cancel()
                    return
                }
                if (player.floor() != startPos) {
                    val title = TextFormat.colorize(MessageHandler.getMessage("servers_teleport_cancelled_title"))
                    val subtitle = TextFormat.colorize(MessageHandler.getMessage("servers_teleport_cancelled_subtitle"))
                    player.sendTitle(title, subtitle, 0, 40, 10)
                    MessageHandler.sendMessage(player, "servers_teleport_cancelled")
                    cancel()
                    return
                }

                if (count > 0) {
                    val title = TextFormat.colorize(MessageHandler.getMessage("servers_teleport_countdown_title"))
                    val subtitle = TextFormat.colorize(
                        MessageHandler.getMessage("servers_teleport_countdown_subtitle", "{count}", count.toString())
                    )
                    player.sendTitle(title, subtitle, 0, 20, 0)
                    count--
                } else {
                    val pk = TransferPacket().apply {
                        address = entry.ip
                        port = entry.port
                    }
                    player.dataPacket(pk)
                    MessageHandler.sendMessage(
                        player, "servers_connecting",
                        "{server}", entry.name,
                        "{ip}", entry.ip,
                        "{port}", entry.port.toString()
                    )
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0, 20)
    }

    data class ServerEntry(val name: String, val ip: String, val port: Int)

    companion object {
        private lateinit var instance: ServersCommand
        fun getInstance(): ServersCommand = instance
    }
}
