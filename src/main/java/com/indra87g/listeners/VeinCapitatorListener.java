package com.indra87g.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;

public class VeinCapitatorListener implements Listener {

    private final Plugin plugin;
    private Set<String> treeLogs = new HashSet<>();
    private Set<String> ores = new HashSet<>();
    private int maxBlocks;

    public VeinCapitatorListener(Plugin plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    private void loadSettings() {
        File configFile = new File(plugin.getDataFolder(), "autobreak.yml");
        if (!configFile.exists()) {
            plugin.saveResource("autobreak.yml", false);
        }

        Config config = new Config(configFile, Config.YAML);

        treeLogs.clear();
        ores.clear();

        treeLogs.addAll(config.getStringList("tree-logs"));
        ores.addAll(config.getStringList("ores"));

        this.maxBlocks = config.getInt("max-blocks", 200);
        plugin.getLogger().info("AutoBreak loaded: " + treeLogs.size() + " tree logs, " + ores.size() + " ores.");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("waffle.autobreak")) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }

        Block block = event.getBlock();

        if (isTreeLog(block) || isOre(block)) {
            event.setCancelled(true);

            Server.getInstance().getScheduler().scheduleTask(plugin, () -> {
                Set<Block> collected = collectBlocks(block, isTreeLog(block));
                int total = collected.size();

                if (total == 0) return;

                for (Block b : collected) {
                    b.getLevel().setBlock(b, Block.get(Block.AIR));
                    Item drop = getDropFor(b);
                    if (drop != null) {
                        player.getInventory().addItem(drop);
                    }
                }
                player.sendMessage("§aAuto-mined " + total + " blocks!");
            });
        }
    }

    private boolean isTreeLog(Block block) {
        return treeLogs.contains(block.getId() + ":" + block.getDamage());
    }

    private boolean isOre(Block block) {
        return ores.contains(block.getId() + ":" + block.getDamage()) || ores.contains(String.valueOf(block.getId()));
    }

    private Set<Block> collectBlocks(Block start, boolean treeMode) {
        Set<Block> result = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty() && result.size() < maxBlocks) {
            Block current = queue.poll();
            String key = current.getX() + "," + current.getY() + "," + current.getZ();
            if (visited.contains(key)) continue;
            visited.add(key);

            if ((treeMode && isTreeLog(current)) || (!treeMode && isOre(current))) {
                result.add(current);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Block neighbor = current.getLevel().getBlock(current.add(dx, dy, dz));
                            if (!visited.contains(neighbor.getX() + "," + neighbor.getY() + "," + neighbor.getZ())) {
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private Item getDropFor(Block block) {
        switch (block.getId()) {
            case 14: // Gold ore
                return Item.get(Item.GOLD_INGOT, 0, 1);
            case 15: // Iron ore
                return Item.get(Item.IRON_INGOT, 0, 1);
            case 16: // Coal ore
                return Item.get(Item.COAL, 0, 1 + new Random().nextInt(2)); // 1-2 coal
            case 21: // Lapis Lazuli ore
                return Item.get(Item.DYE, 4, 4 + new Random().nextInt(5)); // 4-8 lapis
            case 56: // Diamond ore
                return Item.get(Item.DIAMOND, 0, 1);
            case 73: // Redstone ore
            case 74: // Glowing Redstone ore
                return Item.get(Item.REDSTONE, 0, 4 + new Random().nextInt(2)); // 4-5 redstone
            case 129: // Emerald ore
                return Item.get(Item.EMERALD, 0, 1);
            case 153: // Nether Quartz ore
                return Item.get(Item.NETHER_QUARTZ, 0, 1);
            default:
                return block.toItem(); 
        }
    }
}
