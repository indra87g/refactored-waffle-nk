package com.indra87g.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinCapitatorListener implements Listener {

    private final Plugin plugin;
    private Set<Integer> treeLogs = new HashSet<>();
    private Set<Integer> ores = new HashSet<>();
    private int maxBlocks;

    public VeinCapitatotListener(Plugin plugin) {
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

        List<Integer> treeList = config.getIntegerList("tree-logs");
        List<Integer> oreList = config.getIntegerList("ores");

        treeLogs.addAll(treeList);
        ores.addAll(oreList);

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
                Set<Block> collected = new HashSet<>();
                collectBlocks(block, collected, isTreeLog(block));

                for (Block b : collected) {
                    if (b.getId() != Block.AIR) {
                        b.getLevel().useBreakOn(b, player.getInventory().getItemInHand(), player, true);
                    }
                }
                player.sendMessage("§aAuto-mined " + collected.size() + " blocks!");
            });
        }
    }

    private boolean isTreeLog(Block block) {
        return treeLogs.contains(block.getId());
    }

    private boolean isOre(Block block) {
        return ores.contains(block.getId());
    }

    private void collectBlocks(Block start, Set<Block> set, boolean treeMode) {
        if (set.size() >= maxBlocks) return;
        if (set.contains(start)) return;

        if ((treeMode && isTreeLog(start)) || (!treeMode && isOre(start))) {
            set.add(start);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block neighbor = start.getLevel().getBlock(start.add(dx, dy, dz));
                        collectBlocks(neighbor, set, treeMode);
                    }
                }
            }
        }
    }
}
