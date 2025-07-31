package com.indra87g.commands;

import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.Player;

public class SetBlockCommand extends Command {

    public SetBlockCommand() {
        super("setblock");
        
        this.setDescription("Place a block at specific location");
        this.setUsage("/setblock <x> <y> <z> <block_id>");
        
        this.setPermission("waffle.setblock");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.isPlayer(sender)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return false;
        }

        Player player = (Player) sender;
        
        if (!this.validateArguments(args, player)) {
            return false;
        }
      
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            int blockId = Integer.parseInt(args[3]);

            if (!this.validateCoordinates(x, y, z, player)) {
                return false;
            }
      
            this.placeBlock(player, x, y, z, blockId);
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cError: All arguments must be valid numbers.");
            player.sendMessage("§eUsage: " + this.getUsage());
            return false;
        }

        return true;
    }
    
    private boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }
  
    private boolean validateArguments(String[] args, Player player) {
        if (args.length != 4) {
            player.sendMessage("§cError: Wrong number of arguments.");
            player.sendMessage("§eUsage: " + this.getUsage());
            return false;
        }
        return true;
    }
    
    private boolean validateCoordinates(int x, int y, int z, Player player) {
        if (y < 0 || y > 255) {
            player.sendMessage("§cError: Y coordinate must be between 0 and 255.");
            return false;
        }
        return true;
    }
  
    private void placeBlock(Player player, int x, int y, int z, int blockId) {
        Level level = player.getLevel();
        Vector3 position = new Vector3(x, y, z);
        Block block = Block.get(blockId);
        
        if (block.getId() == 0 && blockId != 0) { // 0 = Air
            player.sendMessage("§cError: Invalid block ID: " + blockId);
            return;
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
        } else {
            player.sendMessage("§cError: Failed to place block. The area might be protected.");
        }
    }
  }
