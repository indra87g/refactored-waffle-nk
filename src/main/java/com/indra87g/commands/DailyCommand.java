package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.indra87g.daily.DailyRewardManager;

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
            player.sendMessage(TextFormat.YELLOW + "Usage: /daily <claim|status>");
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
                player.sendMessage(TextFormat.YELLOW + "Usage: /daily <claim|status>");
                break;
        }
        return true;
    }

    private void sendStatus(Player player) {
        int streak = rewardManager.getStreak(player);
        ZonedDateTime now = rewardManager.getNow();
        String today = now.format(DateTimeFormatter.ofPattern("EEEE"));
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        player.sendMessage(TextFormat.GOLD + "===== Daily Reward Status =====");
        player.sendMessage(TextFormat.GREEN + "Today: " + TextFormat.WHITE + today + " (" + date + ")");
        player.sendMessage(TextFormat.GREEN + "Current Streak: " + TextFormat.WHITE + streak + " days");

        if (rewardManager.hasSpecialReward(date)) {
            player.sendMessage(TextFormat.AQUA + "Special reward is available today!");
        } else {
            player.sendMessage(TextFormat.GRAY + "No special reward today.");
        }
    }
}
