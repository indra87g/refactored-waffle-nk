package com.indra87g;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.Player;

public class Main extends PluginBase {

    @Override
    public void onEnable() {
        getLogger().info("plugin activated!");

        this.getServer().getCommandMap().register("setblock", new Command("setblock") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be used by player.");
                    return false;
                }

                Player player = (Player) sender;
                Level level = player.getLevel();

                if (args.length != 4) {
                    player.sendMessage("§cusage: /setblock <x> <y> <z> <block_id>");
                    return false;
                }

                try {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    int z = Integer.parseInt(args[2]);
                    int blockId = Integer.parseInt(args[3]);

                    Vector3 pos = new Vector3(x, y, z);
                    Block block = Block.get(blockId);

                    level.setBlock(pos, block);
                    player.sendMessage("§aBlock " + block.getName() + " successfully placed in  (" + x + ", " + y + ", " + z + ")");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cMake sure all arguments are valid numbers. ");
                }

                return true;
            }
        });
    }
}
