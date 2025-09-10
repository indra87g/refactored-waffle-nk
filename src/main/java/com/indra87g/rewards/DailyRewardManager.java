package com.indra87g.rewards;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyRewardManager {
    private final Config dataConfig;
    private final Config mainConfig;
    private final Map<String, String> messages = new HashMap<>();

    private final ZoneId zoneId;
    private final LocalTime resetTime;

    public DailyRewardManager(File configFile, File dataFolder) {
        this.mainConfig = new Config(configFile, Config.YAML);

        File dataFile = new File(dataFolder, "players.yml");
        this.dataConfig = new Config(dataFile, Config.YAML);

        this.zoneId = ZoneId.of(mainConfig.getString("timezone", "UTC"));
        this.resetTime = LocalTime.parse(mainConfig.getString("claim_reset", "00:00"));

        // Load messages from the 'messages' section of daily_rewards.yml
        if (mainConfig.get("messages") instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) mainConfig.get("messages")).entrySet()) {
                if (entry.getValue() instanceof String) {
                    messages.put(entry.getKey(), (String) entry.getValue());
                }
            }
        }
    }

    public boolean claimReward(Player player) {
        String name = player.getName().toLowerCase();
        long lastClaimSeconds = dataConfig.getLong(name + ".last_claim", 0);
        int streak = dataConfig.getInt(name + ".streak", 0);

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime lastReset = getLastReset(now);

        if (lastClaimSeconds >= lastReset.toEpochSecond()) {
            ZonedDateTime nextReset = lastReset.plusDays(1);
            long secondsRemaining = now.until(nextReset, ChronoUnit.SECONDS);
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            String timeRemaining = String.format("%dh %dm", hours, minutes);
            sendMessage(player, "already_claimed", "{time}", timeRemaining);
            return false;
        }

        ZonedDateTime previousReset = lastReset.minusDays(1);
        if (lastClaimSeconds >= previousReset.toEpochSecond()) {
            streak++;
        } else {
            streak = 1;
        }

        List<List<Map>> rewardsList = (List<List<Map>>) mainConfig.getList("rewards");
        if (rewardsList.isEmpty()) {
            sendMessage(player, "not_available");
            return false;
        }

        dataConfig.set(name + ".last_claim", now.toEpochSecond());
        dataConfig.set(name + ".streak", streak);
        dataConfig.save();

        int rewardIndex = (streak - 1) % rewardsList.size();
        List<Map> rewards = rewardsList.get(rewardIndex);

        giveRewards(player, rewards);
        sendMessage(player, "success", "{day}", String.valueOf(streak));

        if (streak == rewardsList.size()) {
            sendMessage(player, "rewards_claimed");
        }

        return true;
    }

    private void giveRewards(Player player, List<Map> rewards) {
        if (rewards == null) return;
        for (Map reward : rewards) {
            String type = reward.get("type").toString().toLowerCase();
            switch (type) {
                case "item":
                    int id = Integer.parseInt(reward.get("id").toString());
                    int meta = reward.containsKey("meta") ? Integer.parseInt(reward.get("meta").toString()) : 0;
                    int amount = Integer.parseInt(reward.get("amount").toString());
                    player.getInventory().addItem(Item.get(id, meta, amount));
                    break;
                case "money":
                    double money = Double.parseDouble(reward.get("amount").toString());
                    EconomyAPI.getInstance().addMoney(player, money);
                    break;
                case "effect":
                    Effect effect = Effect.getEffectByName(reward.get("effect").toString().toUpperCase());
                    int duration = Integer.parseInt(reward.get("duration").toString());
                    int amplifier = Integer.parseInt(reward.get("amplifier").toString());
                    if (effect != null) {
                        effect.setDuration(duration * 20);
                        effect.setAmplifier(amplifier - 1); // Amplifiers are 0-indexed in code
                        player.addEffect(effect);
                    }
                    break;
                case "exp":
                    int exp = Integer.parseInt(reward.get("amount").toString());
                    player.addExperience(exp);
                    break;
                case "command":
                    String cmd = reward.get("command").toString().replace("%player%", player.getName());
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), cmd);
                    break;
            }
        }
    }

    public void sendMessage(Player player, String key, String... replacements) {
        String message = messages.getOrDefault(key, "&cMessage not found in daily_rewards.yml: " + key);
        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        player.sendMessage(TextFormat.colorize(message));
    }

    public ZonedDateTime getLastReset(ZonedDateTime now) {
        ZonedDateTime todayReset = now.with(resetTime);
        return now.isBefore(todayReset) ? todayReset.minusDays(1) : todayReset;
    }

    public int getStreak(Player player) {
        return dataConfig.getInt(player.getName().toLowerCase() + ".streak", 0);
    }

    public ZonedDateTime getNextResetTime() {
        return getLastReset(ZonedDateTime.now(zoneId)).plusDays(1);
    }

    public long getLastClaimTime(Player player) {
        return dataConfig.getLong(player.getName().toLowerCase() + ".last_claim", 0);
    }
}
