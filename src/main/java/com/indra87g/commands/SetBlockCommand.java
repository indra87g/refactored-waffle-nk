package com.indra87g.commands;

import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.Player;

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
            player.sendMessage("§cError: Wrong number of arguments.");
            player.sendMessage("§eUsage: " + this.getUsage());
            return false;
        }

        try {
            Integer.parseInt(args[0]);
            Integer.parseInt(args[1]);
            Integer.parseInt(args[2]);
            Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cError: All arguments must be valid numbers.");
            player.sendMessage("§eUsage: " + this.getUsage());
            return false;
        }

        int y = Integer.parseInt(args[1]);
        if (y < 0 || y > 255) {
            player.sendMessage("§cError: Y coordinate must be between 0 and 255.");
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

        if (block.getId() == 0 && blockId != 0) { // 0 = Air
            player.sendMessage("§cError: Invalid block ID: " + blockId);
            return false;
        }

        Block oldBlock = level.getBlock(position);
        boolean success = level.setBlock(position, block);

        if (success) {
            player.sendMessage("§aSuccess! Placed §f" + block.getName() +
                "§a at coordinates §f(" + x + ", " + y + ", " + z + ")");

            player.getServer().getLogger().info(
                player.getName() + " placed " + block.getName() +
                " at (" + x + ", " + y + ", " + z + ") in world " + level.getName()
            );
            return true;
        } else {
            player.sendMessage("§cError: Failed to place block. The area might be protected.");
            return false;
        }
    }
}
