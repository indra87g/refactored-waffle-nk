package com.indra87g.rewards;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class DailyRewardManager {

    private final Config mainConfig;
    private final Config dataConfig;

    public DailyRewardManager(File configFile, File dataFolder) {
        this.mainConfig = new Config(configFile, Config.YAML);
        File dataFile = new File(dataFolder, "daily_data.yml");
        this.dataConfig = new Config(dataFile, Config.YAML);
    }

    public boolean claimReward(Player player) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        int day = now.getDayOfWeek().getValue();
        String path = "rewards.reward" + day;

        List<Map<String, Object>> rewardsForToday = (List<Map<String, Object>>) mainConfig.getList(path);
        if (rewardsForToday == null || rewardsForToday.isEmpty()) {
            player.sendMessage(colorize(mainConfig.getString("messages.not_available", "&cNo reward today.")));
            return false;
        }

        String playerPath = "players." + player.getUniqueId().toString() + ".lastClaim";
        String lastClaim = dataConfig.getString(playerPath, "");
        String today = now.toLocalDate().toString();

        if (today.equals(lastClaim)) {
            player.sendMessage(colorize(mainConfig.getString("messages.already_claimed", "&cYou already claimed today's reward.")));
            return false;
        }

        dataConfig.set(playerPath, today);
        dataConfig.save();

        for (Map<String, Object> reward : rewardsForToday) {
            String type = ((String) reward.get("type")).toLowerCase();

            switch (type) {
                case "item": {
                    int id = (int) reward.get("id");
                    int amount = (int) reward.getOrDefault("amount", 1);
                    player.getInventory().addItem(Item.get(id, 0, amount));
                    break;
                }
                case "money": {
                    int money = (int) reward.get("amount");
                    EconomyAPI.getInstance().addMoney(player, money);
                    break;
                }
                case "effect": {
                    String effectName = (String) reward.get("effect");
                    int duration = (int) reward.getOrDefault("duration", 60);
                    int amplifier = (int) reward.getOrDefault("amplifier", 0);
                    Effect effect = Effect.getEffectByName(effectName.toUpperCase());
                    if (effect != null) {
                        effect.setDuration(duration * 20);
                        effect.setAmplifier(amplifier);
                        player.addEffect(effect);
                    }
                    break;
                }
                case "exp": {
                    int exp = (int) reward.get("amount");
                    player.addExperience(exp);
                    break;
                }
                case "command": {
                    String cmd = (String) reward.get("command");
                    player.getServer().dispatchCommand(
                        player.getServer().getConsoleSender(),
                        cmd.replace("%player%", player.getName())
                    );
                    break;
                }
            }
        }

        player.sendMessage(colorize(
            mainConfig.getString("messages.success", "&aYou claimed reward for day %day%!")
                .replace("%day%", String.valueOf(day))
        ));

        return true;
    }

    public int getStreak(Player player) {
        String path = "players." + player.getUniqueId().toString() + ".streak";
        return dataConfig.getInt(path, 0);
    }

    public ZonedDateTime getNow() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }

    public boolean hasSpecialReward(String date) {
        return mainConfig.exists("special." + date);
    }

    private String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }
}
