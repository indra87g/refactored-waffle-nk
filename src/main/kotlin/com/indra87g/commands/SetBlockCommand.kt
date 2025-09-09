package com.indra87g.commands

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.math.Vector3
import com.indra87g.utils.MessageHandler

class SetBlockCommand : BaseCommand(
    "setblock",
    "Place a block at specific location",
    "/setblock <x> <y> <z> <block_id>",
    "waffle.setblock"
) {

    override fun validateArgs(args: Array<String>, player: Player): Boolean {
        if (args.size != 4) {
            MessageHandler.sendMessage(player, "setblock_invalid_arg_count")
            MessageHandler.sendMessage(player, "invalid_usage", "{usage}", usageMessage)
            return false
        }

        val y = args[1].toIntOrNull()
        if (args[0].toIntOrNull() == null || y == null || args[2].toIntOrNull() == null || args[3].toIntOrNull() == null) {
            MessageHandler.sendMessage(player, "setblock_invalid_arg_type")
            MessageHandler.sendMessage(player, "invalid_usage", "{usage}", usageMessage)
            return false
        }

        if (y !in 0..255) {
            MessageHandler.sendMessage(player, "setblock_invalid_y")
            return false
        }
        return true
    }

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        val x = args[0].toInt()
        val y = args[1].toInt()
        val z = args[2].toInt()
        val blockId = args[3].toInt()

        val position = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
        val block = Block.get(blockId)

        if (block.id == Block.AIR && blockId != 0) {
            MessageHandler.sendMessage(player, "setblock_invalid_block_id", "{id}", blockId.toString())
            return false
        }

        val success = player.level.setBlock(position, block, true, true)

        if (success) {
            MessageHandler.sendMessage(
                player, "setblock_success",
                "{block}", block.name,
                "{x}", x.toString(),
                "{y}", y.toString(),
                "{z}", z.toString()
            )
            player.server.logger.info(
                "${player.name} placed ${block.name} at ($x, $y, $z) in world ${player.level.name}"
            )
        } else {
            MessageHandler.sendMessage(player, "setblock_failure")
        }
        return true
    }
}
