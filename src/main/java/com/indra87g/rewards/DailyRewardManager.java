package com.indra87g.rewards;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DailyRewardManager {
    private final Config claimData;
    private final Config config;
    private final String dataFileName = "daily_claims.yml";
    private final String configFileName = "daily_rewards.yml";

    public DailyRewardManager(String dataFolder) {
        File dataFile = new File(dataFolder, dataFileName);
        File configFile = new File(dataFolder, configFileName);
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (Exception ignored) {}
        }
        this.claimData = new Config(dataFile, Config.YAML);
        this.config = new Config(configFile, Config.YAML);
    }

    @SuppressWarnings("unchecked")
    public String claim(Player player) {
        String name = player.getName();
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        String lastClaim = claimData.getString(name + ".last_claim", "");
        if (todayStr.equals(lastClaim)) {
            return getMsg("already_claimed");
        }

        int dayOfWeek = today.getDayOfWeek().getValue(); // 1-7
        String rewardKey = "reward" + dayOfWeek;

        List<Map<String, Object>> rewardList = (List<Map<String, Object>>) config.getNested("rewards." + rewardKey);
        if (rewardList == null) {
            return getMsg("not_available");
        }

        for (Map<String, Object> reward : rewardList) {
            String type = (String) reward.get("type");
            if ("item".equalsIgnoreCase(type)) {
                int id = (int) reward.getOrDefault("id", 1);
                int amount = (int) reward.getOrDefault("amount", 1);
                Item item = Item.get(id, 0, amount);
                player.getInventory().addItem(item);
            } else if ("money".equalsIgnoreCase(type)) {
                double amount = Double.parseDouble(reward.get("amount").toString());
                EconomyAPI.getInstance().addMoney(player, amount);
            } else if ("effect".equalsIgnoreCase(type)) {
                String effectStr = (String) reward.getOrDefault("effect", "SPEED");
                int duration = (int) reward.getOrDefault("duration", 600);
                int amplifier = (int) reward.getOrDefault("amplifier", 1);
                Effect effect = Effect.getEffectByName(effectStr);
                if (effect != null) {
                    effect.setDuration(duration * 20);
                    effect.setAmplifier(amplifier);
                    player.addEffect(effect);
                }
            } else if ("exp".equalsIgnoreCase(type)) {
                int amount = (int) reward.getOrDefault("amount", 1);
                player.addExperience(amount);
            }
        }

        claimData.set(name + ".last_claim", todayStr);
        claimData.save();

        String msg = getMsg("success");
        msg = msg.replace("%day%", String.valueOf(dayOfWeek));
        return msg;
    }

    public boolean canClaim(Player player) {
        String name = player.getName();
        String todayStr = LocalDate.now().toString();
        String lastClaim = claimData.getString(name + ".last_claim", "");
        return !todayStr.equals(lastClaim);
    }

    private String getMsg(String key) {
        return config.getSection("messages").getString(key, "Success!");
    }
}
