package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import me.onebone.economyapi.EconomyAPI;
import java.util.Random;

public class CasinoCommand extends BaseCommand {

    private final Random random = new Random();

    public CasinoCommand() {
        super("casino", "Play casino game", "/casino <coinflip|slot|dice> ...");
        this.setPermission("waffle.casino");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command only for players!");
            return false;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§eUsage: /casino <coinflip|slot|dice>");
            return false;
        }

        String game = args[0].toLowerCase();

        switch (game) {
            case "coinflip":
                return handleCoinFlip(player, args);
            case "slot":
                return handleSlot(player, args);
            case "dice":
                return handleDice(player, args);
            default:
                player.sendMessage("§cGame not found! Use: coinflip, slot, dice");
                return false;
        }
    }

    private boolean handleCoinFlip(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§eUsage: /casino coinflip <heads|tails> <amount>");
            return false;
        }

        String choice = args[1].toLowerCase();
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid bet amount!");
            return false;
        }

        if (!choice.equals("heads") && !choice.equals("tails")) {
            player.sendMessage("§cSelection must be 'heads' or 'tails'!");
            return false;
        }

        EconomyAPI eco = EconomyAPI.getInstance();
        if (eco.myMoney(player) < amount) {
            player.sendMessage("§cYour money is not enough!");
            return false;
        }

        String result = random.nextBoolean() ? "heads" : "tails";
        if (choice.equals(result)) {
            eco.addMoney(player, amount);
            player.sendMessage("§aThe coin fell on " + result + "! You won " + amount + "!");
        } else {
            eco.reduceMoney(player, amount);
            player.sendMessage("§cThe coin fell on " + result + ". You lose " + amount + "!");
        }
        return true;
    }

    private boolean handleSlot(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§eUsage: /casino slot <amount>");
            return false;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid bet amount!");
            return false;
        }

        EconomyAPI eco = EconomyAPI.getInstance();
        if (eco.myMoney(player) < amount) {
            player.sendMessage("§cYour money is not enough!");
         return false;
        }

        String[] symbols = {"○", "□", "♡", "◇"};
        String s1 = symbols[random.nextInt(symbols.length)];
        String s2 = symbols[random.nextInt(symbols.length)];
        String s3 = symbols[random.nextInt(symbols.length)];

        player.sendMessage("§e[ " + s1 + " | " + s2 + " | " + s3 + " ]");

        if (s1.equals(s2) && s2.equals(s3)) {
            eco.addMoney(player, amount * 5);
            player.sendMessage("§aTriple! You won " + (amount * 5) + "!");
        } else if (s1.equals(s2) || s2.equals(s3) || s1.equals(s3)) {
            eco.addMoney(player, amount * 2);
            player.sendMessage("§aTwo of kind ! You won " + (amount * 2) + "!");
        } else {
            eco.reduceMoney(player, amount);
            player.sendMessage("§cNothing matches. You lose " + amount + "!");
        }
        return true;
    }

    private boolean handleDice(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§eUsage: /casino dice <1-6> <amount>");
            return false;
        }

        int guess;
        double amount;
        try {
            guess = Integer.parseInt(args[1]);
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid input!");
            return false;
        }

        if (guess < 1 || guess > 6) {
            player.sendMessage("§cGuess must be between 1-6!");
            return false;
        }

        EconomyAPI eco = EconomyAPI.getInstance();
        if (eco.myMoney(player) < amount) {
            player.sendMessage("§cYour money is not enough!");
            return false;
        }

        int roll = random.nextInt(6) + 1;
        player.sendMessage("§eDice show the number: " + roll);

        if (roll == guess) {
            eco.addMoney(player, amount * 6);
            player.sendMessage("§aCorrect guess! You won " + (amount * 6) + "!");
        } else {
            eco.reduceMoney(player, amount);
            player.sendMessage("§cWrong guess. You lose " + amount + "!");
        }
        return true;
    }
}
