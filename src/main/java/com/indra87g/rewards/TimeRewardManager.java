package com.indra87g.rewards;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.indra87g.listeners.ActivityListener;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeRewardManager {
    private final PluginBase plugin;
    private int afkTimeout;
    private final Map<String, Long> lastActivity = new ConcurrentHashMap<>();
    private final List<RewardSchedule> schedules = new ArrayList<>();
    private final Map<String, Map<String, Long>> playerRewardHistory = new ConcurrentHashMap<>();

    private static class RewardSchedule {
        String id;
        int interval; // in seconds
        double reward;
        String message;
    }

    public TimeRewardManager(PluginBase plugin) {
        this.plugin = plugin;
        loadConfigAndStart();
        plugin.getServer().getPluginManager().registerEvents(
            new ActivityListener(lastActivity, playerRewardHistory), plugin
        );
    }

    @SuppressWarnings("unchecked")
    private void loadConfigAndStart() {
        File file = new File(plugin.getDataFolder(), "time_rewards.yml");
        if (!file.exists()) {
            plugin.saveResource("time_rewards.yml", false);
        }
        Config config = new Config(file, Config.YAML);
        afkTimeout = config.getInt("afk_timeout", 300);

        List<Map<String, Object>> scheduleData = (List<Map<String, Object>>) config.get("schedules");
        if (scheduleData == null) return;

        int i = 0;
        for (Map<String, Object> s : scheduleData) {
            RewardSchedule sch = new RewardSchedule();
            sch.id = (String) s.getOrDefault("id", "reward" + (++i));
            sch.interval = (int) s.getOrDefault("interval", 300);
            sch.reward = Double.parseDouble(s.get("reward").toString());
            sch.message = (String) s.getOrDefault("message", "Kamu menerima " + sch.reward + " uang karena bermain!");
            schedules.add(sch);
        }

        startMasterRewardTask();
    }

    private void startMasterRewardTask() {
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                long lastPlayerActivity = lastActivity.getOrDefault(player.getName(), 0L);
                if ((now - lastPlayerActivity) / 1000 >= afkTimeout) {
                    continue;
                }

                for (RewardSchedule sch : schedules) {
                    Map<String, Long> playerHistory = playerRewardHistory.computeIfAbsent(player.getName(), k -> {
                        Map<String, Long> history = new HashMap<>();
                        // For a new player, set their last reward time to now, so the cooldown starts now.
                        for(RewardSchedule schedule : schedules) {
                            history.put(schedule.id, now);
                        }
                        return history;
                    });

                    long lastRewardTime = playerHistory.getOrDefault(sch.id, now);

                    if ((now - lastRewardTime) / 1000 >= sch.interval) {
                        EconomyAPI.getInstance().addMoney(player, sch.reward);
                        player.sendMessage("§a" + sch.message);
                        playerHistory.put(sch.id, now);
                    }
                }
            }
        }, 20); // Run every second
    }
}
