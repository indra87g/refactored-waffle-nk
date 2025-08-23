package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.rewards.DailyRewardManager;

public class DailyCommand extends BaseCommand {
    private final DailyRewardManager rewardManager;

    public DailyCommand(DailyRewardManager rewardManager) {
        super("daily", "Claim your daily reward", "/daily", "waffle.daily");
        this.rewardManager = rewardManager;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String msg = rewardManager.claim(player);
        player.sendMessage("§a" + msg);
        return true;
    }
}
