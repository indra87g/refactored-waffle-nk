package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.rewards.DailyRewardManager;
import com.indra87g.utils.MessageHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DailyCommand extends BaseCommand {
    private final DailyRewardManager rewardManager;

    public DailyCommand(DailyRewardManager rewardManager) {
        super("daily", "Daily rewards system", "/daily <claim|status>", "waffle.daily");
        this.rewardManager = rewardManager;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            MessageHandler.sendMessage(player, "daily_usage");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "claim":
                rewardManager.claimReward(player);
                break;

            case "status":
                sendStatus(player);
                break;

            default:
                MessageHandler.sendMessage(player, "daily_usage");
                break;
        }
        return true;
    }

    private void sendStatus(Player player) {
        int streak = rewardManager.getStreak(player);
        ZonedDateTime now = rewardManager.getNow();
        String today = now.format(DateTimeFormatter.ofPattern("EEEE"));
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MessageHandler.sendDirectMessage(player, MessageHandler.getMessage("daily_status_header"));
        MessageHandler.sendMessage(player, "daily_status_today", "{day}", today, "{date}", date);
        MessageHandler.sendMessage(player, "daily_status_streak", "{streak}", String.valueOf(streak));

        if (rewardManager.hasSpecialReward(date)) {
            MessageHandler.sendMessage(player, "daily_status_special_reward");
        } else {
            MessageHandler.sendMessage(player, "daily_status_no_special_reward");
        }
    }
}
