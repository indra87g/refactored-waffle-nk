package com.indra87g.commands;

import cn.nukkit.Player;
import me.onebone.economyapi.EconomyAPI;

import java.util.Random;

public class CasinoCommand extends BaseCommand {

    public CasinoCommand() {
        super("casino", "Random money bets", "/casino <amount>", "waffle.casino");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage("§cWrong usage!");
            player.sendMessage("§eUsage: " + getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        int amount;

        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cThe bet amount must be a number!");
            return false;
        }

        if (amount <= 0) {
            player.sendMessage("§cThe bet amount must be more than 0! .");
            return false;
        }

        EconomyAPI economy = EconomyAPI.getInstance();
        double balance = economy.myMoney(player);

        if (balance < amount) {
            player.sendMessage("§cYour money is not enough. Balance: §f" + balance);
            return false;
        }

        economy.reduceMoney(player, amount);
        player.sendMessage("§7The betting begins...");

        Random rand = new Random();
        int roll = rand.nextInt(100); // 0 - 99

        if (roll < 40) { 
            player.sendMessage("§cToo bad, you lost. Your money is gone :) ");
        } else if (roll < 50) {
            economy.addMoney(player, amount);
            player.sendMessage("§eYou're a novice gambler, you'd better not play!");
        } else if (roll < 25) {
            int winAmount = amount * 10;
            economy.addMoney(player, winAmount);
            player.sendMessage("§l§gJACKPOT!§rYou won and got §f" + winAmount);
        } else {
            int winAmount = amount * 2;
            economy.addMoney(player, winAmount);
            player.sendMessage("§aCongratulations! You won and got §f" + winAmount + "§a!");
        }

        return true;
    }
}
