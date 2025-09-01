package com.indra87g.rewards;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
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

        long lastClaim = dataConfig.getLong(name + ".last_claim", 0);
        int streak = dataConfig.getInt(name + ".streak", 0);

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime lastReset = getResetTime(now);
        ZonedDateTime prevReset = lastReset.minusDays(1);

        boolean alreadyClaimed = lastClaim >= lastReset.toEpochSecond();
        if (alreadyClaimed) {
            player.sendMessage(mainConfig.getString("messages.already_claimed"));
            return false;
        }

        if (lastClaim >= prevReset.toEpochSecond() && lastClaim < lastReset.toEpochSecond()) {
            streak++;
        } else {
            streak = 1;
        }

        dataConfig.set(name + ".last_claim", now.toEpochSecond());
        dataConfig.set(name + ".streak", streak);
        dataConfig.save();

        boolean rewardGiven = false;

        String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (mainConfig.exists("special_rewards." + today)) {
            giveRewards(player, mainConfig.getMapList("special_rewards." + today));
            player.sendMessage(mainConfig.getString("messages.special_reward")
                    .replace("%date%", today));
            rewardGiven = true;
        }

        int dayOfWeek = now.getDayOfWeek().getValue(); // Monday=1, Sunday=7
        String rewardKey = "rewards.reward" + dayOfWeek;
        if (mainConfig.isList(rewardKey)) {
            giveRewards(player, mainConfig.getMapList(rewardKey));
            player.sendMessage(mainConfig.getString("messages.success")
                    .replace("%day%", now.getDayOfWeek().toString()));
            rewardGiven = true;
        }

        if (mainConfig.exists("streak_rewards." + streak)) {
            giveRewards(player, mainConfig.getMapList("streak_rewards." + streak));
            player.sendMessage(mainConfig.getString("messages.streak_bonus")
                    .replace("%streak%", String.valueOf(streak)));
            rewardGiven = true;
        }

        if (!rewardGiven) {
            player.sendMessage(mainConfig.getString("messages.not_available"));
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
