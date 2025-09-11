package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.utils.MessageHandler;
import me.onebone.economyapi.EconomyAPI;
import java.util.Random;

public class CasinoCommand extends BaseCommand {

    private final Random random = new Random();
    private final EconomyAPI economyAPI = EconomyAPI.getInstance();

    public CasinoCommand() {
        super("casino", "Play casino game", "/casino <coinflip|slot|dice> ...", "waffle.casino");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length < 1) {
            MessageHandler.sendMessage(player, "casino_usage");
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        String game = args[0].toLowerCase();

        switch (game) {
            case "coinflip":
                return handleCoinFlip(player, args);
            case "slot":
                return handleSlot(player, args);
            case "dice":
                return handleDice(player, args);
            default:
                MessageHandler.sendMessage(player, "casino_game_not_found");
                return false;
        }
    }

    private double handleBet(Player player, String amountStr) {
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                MessageHandler.sendMessage(player, "invalid_bet_amount");
                return -1;
            }
        } catch (NumberFormatException e) {
            MessageHandler.sendMessage(player, "invalid_bet_amount");
            return -1;
        }

        if (economyAPI.myMoney(player) < amount) {
            MessageHandler.sendMessage(player, "not_enough_money");
            return -1;
        }
        return amount;
    }

    private boolean handleCoinFlip(Player player, String[] args) {
        if (args.length < 3) {
            MessageHandler.sendMessage(player, "coinflip_usage");
            return false;
        }

        String choice = args[1].toLowerCase();
        if (!choice.equals("heads") && !choice.equals("tails")) {
            MessageHandler.sendMessage(player, "coinflip_invalid_choice");
            return false;
        }

        double amount = handleBet(player, args[2]);
        if (amount < 0) {
            return false;
        }

        economyAPI.reduceMoney(player, amount);

        String result = random.nextBoolean() ? "heads" : "tails";
        if (choice.equals(result)) {
            double prize = amount * 2;
            economyAPI.addMoney(player, prize);
            MessageHandler.sendMessage(player, "coinflip_win", "{result}", result, "{amount}", String.valueOf(prize));
        } else {
            MessageHandler.sendMessage(player, "coinflip_lose", "{result}", result, "{amount}", String.valueOf(amount));
        }
        return true;
    }

    private boolean handleSlot(Player player, String[] args) {
        if (args.length < 2) {
            MessageHandler.sendMessage(player, "slot_usage");
            return false;
        }

        double amount = handleBet(player, args[1]);
        if (amount < 0) {
            return false;
        }

        economyAPI.reduceMoney(player, amount);

        String[] symbols = {"○", "□", "♡", "◇"};
        String s1 = symbols[random.nextInt(symbols.length)];
        String s2 = symbols[random.nextInt(symbols.length)];
        String s3 = symbols[random.nextInt(symbols.length)];

        MessageHandler.sendMessage(player, "slot_result", "{s1}", s1, "{s2}", s2, "{s3}", s3);

        if (s1.equals(s2) && s2.equals(s3)) {
            double prize = amount * 4;
            economyAPI.addMoney(player, prize);
            MessageHandler.sendMessage(player, "slot_win_triple", "{amount}", String.valueOf(prize));
        } else if (s1.equals(s2) || s2.equals(s3) || s1.equals(s3)) {
            double prize = amount * 1;
            economyAPI.addMoney(player, prize);
            MessageHandler.sendMessage(player, "slot_win_double", "{amount}", String.valueOf(prize));
        } else {
            MessageHandler.sendMessage(player, "slot_lose", "{amount}", String.valueOf(amount));
        }
        return true;
    }

    private boolean handleDice(Player player, String[] args) {
        if (args.length < 3) {
            MessageHandler.sendMessage(player, "dice_usage");
            return false;
        }

        int guess;
        try {
            guess = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            MessageHandler.sendMessage(player, "dice_invalid_guess");
            return false;
        }

        if (guess < 1 || guess > 6) {
            MessageHandler.sendMessage(player, "dice_invalid_guess");
            return false;
        }

        double amount = handleBet(player, args[2]);
        if (amount < 0) {
            return false;
        }

        economyAPI.reduceMoney(player, amount);

        int roll = random.nextInt(6) + 1;
        MessageHandler.sendMessage(player, "dice_result", "{roll}", String.valueOf(roll));

        if (roll == guess) {
            double prize = amount * 5;
            economyAPI.addMoney(player, prize);
            MessageHandler.sendMessage(player, "dice_win", "{amount}", String.valueOf(prize));
        } else {
            MessageHandler.sendMessage(player, "dice_lose", "{amount}", String.valueOf(amount));
        }
        return true;
    }
}
