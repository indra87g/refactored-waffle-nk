package com.indra87g.rewards;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;
import com.indra87g.listeners.ActivityListener;

import java.io.File;
import java.util.*;

public class TimeRewardManager {
    private final PluginBase plugin;
    private int afkTimeout;
    private final Map<String, Long> lastActivity = new HashMap<>();

    private static class RewardSchedule {
        int interval;
        double reward;
        String message;
        String id;
    }

    public TimeRewardManager(PluginBase plugin) {
        this.plugin = plugin;
        loadConfigAndStart();
        plugin.getServer().getPluginManager().registerEvents(
            new ActivityListener(lastActivity), plugin
        );
    }

    @SuppressWarnings("unchecked")
    private void loadConfigAndStart() {
        File file = new File(plugin.getDataFolder(), "time_rewards.yml");
        if (!file.exists()) {
            plugin.saveResource("time_rewards.yml", false);
        }
        Config config = new Config(file, Config.YAML);
        afkTimeout = config.getInt("afk_timeout", 300); // default 5 menit

        List<Map<String, Object>> schedules = (List<Map<String, Object>>) config.get("schedules");
        if (schedules == null) return;
        int i = 0;
        for (Map<String, Object> s : schedules) {
            RewardSchedule sch = new RewardSchedule();
            sch.interval = (int) s.getOrDefault("interval", 300);
            sch.reward = Double.parseDouble(s.get("reward").toString());
            sch.message = (String) s.getOrDefault("message", "Kamu menerima " + sch.reward + " uang karena bermain!");
            sch.id = (String) s.getOrDefault("id", "reward" + (++i));
            startRewardTask(sch);
        }
    }

    private void startRewardTask(RewardSchedule sch) {
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                long last = lastActivity.getOrDefault(player.getName(), now);
                if ((now - last) / 1000 < afkTimeout) {
                    EconomyAPI.getInstance().addMoney(player, sch.reward);
                    player.sendMessage("§a" + sch.message);
                } else {
                    player.sendMessage("§e[AFK] Kamu tidak menerima hadiah (" + sch.id + ") karena sedang AFK.");
                }
            }
        }, sch.interval * 20); // 20 ticks = 1 detik
    }
}
