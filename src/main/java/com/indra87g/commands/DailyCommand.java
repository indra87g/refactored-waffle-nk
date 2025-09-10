package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.indra87g.rewards.DailyRewardManager;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DailyCommand extends BaseCommand {
    private final DailyRewardManager rewardManager;

    public DailyCommand(DailyRewardManager rewardManager) {
        super("daily", "Daily rewards system", "/daily <claim|status>", "waffle.daily");
        this.rewardManager = rewardManager;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            sendUsage(player);
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
                sendUsage(player);
                break;
        }
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(TextFormat.colorize("&6Usage: /daily <claim|status>"));
    }

    private void sendStatus(Player player) {
        int streak = rewardManager.getStreak(player);
        long lastClaimSeconds = rewardManager.getLastClaimTime(player);
        ZonedDateTime now = ZonedDateTime.now(); // Use the same timezone logic as manager if needed, but for status it's minor.
        ZonedDateTime lastReset = rewardManager.getLastReset(now);

        String claimStatus;
        if (lastClaimSeconds >= lastReset.toEpochSecond()) {
            ZonedDateTime nextReset = rewardManager.getNextResetTime();
            long secondsRemaining = now.until(nextReset, ChronoUnit.SECONDS);
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            claimStatus = TextFormat.colorize(String.format("&cClaimed. Next reward in %dh %dm.", hours, minutes));
        } else {
            claimStatus = TextFormat.colorize("&aAvailable! Use /daily claim.");
        }

        player.sendMessage(TextFormat.colorize("&l&eDaily Reward Status&r"));
        player.sendMessage(TextFormat.colorize("&7--------------------"));
        player.sendMessage(TextFormat.colorize("&eCurrent Streak: &f" + streak + " day(s)"));
        player.sendMessage(TextFormat.colorize("&eStatus: " + claimStatus));
        player.sendMessage(TextFormat.colorize("&7--------------------"));
    }
}
