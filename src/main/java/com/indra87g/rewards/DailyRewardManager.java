package com.indra87g.rewards;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import com.indra87g.utils.MessageHandler;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class DailyRewardManager {
    private final Config dataConfig;
    private final Config mainConfig;

    private final ZoneId zoneId;
    private final LocalTime resetTime;

    public DailyRewardManager(File configFile, File dataFolder) {
        this.mainConfig = new Config(configFile, Config.YAML);

        File dataFile = new File(dataFolder, "players.yml");
        this.dataConfig = new Config(dataFile, Config.YAML);

        this.zoneId = ZoneId.of(mainConfig.getString("timezone", "UTC"));
        this.resetTime = LocalTime.parse(mainConfig.getString("claim_reset", "00:00"));
    }

    public boolean claimReward(Player player) {
        String name = player.getName().toLowerCase();

        long lastClaimSeconds = dataConfig.getLong(name + ".last_claim", 0);
        int streak = dataConfig.getInt(name + ".streak", 0);

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime lastReset = getResetTime(now);
        ZonedDateTime prevReset = lastReset.minusDays(1);

        boolean alreadyClaimed = lastClaimSeconds >= lastReset.toEpochSecond();
        if (alreadyClaimed) {
            ZonedDateTime nextReset = lastReset.plusDays(1);
            long secondsRemaining = now.until(nextReset, ChronoUnit.SECONDS);
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            String timeRemaining = String.format("%dh %dm", hours, minutes);
            MessageHandler.sendMessage(player, "daily_reward_already_claimed", "{time}", timeRemaining);
            return false;
        }

        if (lastClaimSeconds >= prevReset.toEpochSecond() && lastClaimSeconds < lastReset.toEpochSecond()) {
            streak++;
        } else {
            streak = 1;
        }

        dataConfig.set(name + ".last_claim", now.toEpochSecond());
        dataConfig.set(name + ".streak", streak);
        dataConfig.save();

        boolean rewardGiven = false;
        String todayDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (mainConfig.exists("special_rewards." + todayDate)) {
            giveRewards(player, mainConfig.getMapList("special_rewards." + todayDate));
            MessageHandler.sendMessage(player, "daily_reward_special", "{date}", todayDate);
            rewardGiven = true;
        }

        int dayOfWeek = now.getDayOfWeek().getValue(); // Monday=1, Sunday=7
        String rewardKey = "rewards.reward" + dayOfWeek;
        if (mainConfig.isList(rewardKey)) {
            giveRewards(player, mainConfig.getMapList(rewardKey));
            String dayName = now.getDayOfWeek().toString();
            MessageHandler.sendMessage(player, "daily_reward_success", "{day}", dayName);
            rewardGiven = true;
        }

        if (mainConfig.exists("streak_rewards." + streak)) {
            giveRewards(player, mainConfig.getMapList("streak_rewards." + streak));
            MessageHandler.sendMessage(player, "daily_reward_streak_bonus", "{streak}", String.valueOf(streak));
            rewardGiven = true;
        }

        if (!rewardGiven) {
            MessageHandler.sendMessage(player, "daily_reward_not_available");
        }

        return rewardGiven;
    }

    private void giveRewards(Player player, List<Map> rewards) {
        for (Map reward : rewards) {
            String type = reward.get("type").toString().toLowerCase();
            switch (type) {
                case "item":
                    int id = Integer.parseInt(reward.get("id").toString());
                    int amount = Integer.parseInt(reward.get("amount").toString());
                    player.getInventory().addItem(new Item(id, 0, amount));
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
                        effect.setAmplifier(amplifier);
                        player.addEffect(effect);
                    }
                    break;
                case "exp":
                    int exp = Integer.parseInt(reward.get("amount").toString());
                    player.addExperience(exp);
                    break;
                case "command":
                    String cmd = reward.get("command").toString()
                            .replace("%player%", player.getName());
                    Server.getInstance().dispatchCommand(
                            Server.getInstance().getConsoleSender(), cmd);
                    break;
            }
        }
    }

    private ZonedDateTime getResetTime(ZonedDateTime now) {
        return now.withHour(resetTime.getHour())
                  .withMinute(resetTime.getMinute())
                  .withSecond(0)
                  .withNano(0);
    }

    public int getStreak(Player player) {
        String name = player.getName().toLowerCase();
        return dataConfig.getInt(name + ".streak", 0);
    }

    public ZonedDateTime getNow() {
        return ZonedDateTime.now(zoneId);
    }

    public boolean hasSpecialReward(String date) {
        return mainConfig.exists("special_rewards." + date);
    }
}
