package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import com.indra87g.utils.MessageHandler;

public class SetBlockCommand extends BaseCommand {

    public SetBlockCommand() {
        super(
            "setblock",
            "Place a block at specific location",
            "/setblock <x> <y> <z> <block_id>",
            "waffle.setblock"
        );
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length != 4) {
            MessageHandler.sendMessage(player, "setblock_invalid_arg_count");
            MessageHandler.sendMessage(player, "invalid_usage", "{usage}", this.getUsage());
            return false;
        }

        try {
            Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            Integer.parseInt(args[2]);
            Integer.parseInt(args[3]);

            if (y < 0 || y > 255) {
                MessageHandler.sendMessage(player, "setblock_invalid_y");
                return false;
            }
        } catch (NumberFormatException e) {
            MessageHandler.sendMessage(player, "setblock_invalid_arg_type");
            MessageHandler.sendMessage(player, "invalid_usage", "{usage}", this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);
        int blockId = Integer.parseInt(args[3]);

        Level level = player.getLevel();
        Vector3 position = new Vector3(x, y, z);
        Block block = Block.get(blockId);

        if (block.getId() == Block.AIR && blockId != 0) {
            MessageHandler.sendMessage(player, "setblock_invalid_block_id", "{id}", String.valueOf(blockId));
            return false;
        }

        boolean success = level.setBlock(position, block, true, true);

        if (success) {
            MessageHandler.sendMessage(player, "setblock_success",
                "{block}", block.getName(),
                "{x}", String.valueOf(x),
                "{y}", String.valueOf(y),
                "{z}", String.valueOf(z)
            );
            player.getServer().getLogger().info(
                player.getName() + " placed " + block.getName() +
                " at (" + x + ", " + y + ", " + z + ") in world " + level.getName()
            );
        } else {
            MessageHandler.sendMessage(player, "setblock_failure");
        }
        return true;
    }
}
