package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.game.MathGameManager;
import com.indra87g.utils.MessageHandler;

public class CalcCommand extends BaseCommand {

    private final MathGameManager gameManager;

    public CalcCommand(MathGameManager gameManager) {
        super("calc", "Do a mathematical calculation or play a math game", "/calc <num1> <op> <num2> | /calc play", "waffle.calc");
        this.gameManager = gameManager;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            MessageHandler.sendMessage(player, "calc_usage"); // Assumes a generic usage message exists
            return true;
        }

        if (args[0].equalsIgnoreCase("play")) {
            gameManager.startGame(player);
            return true;
        }

        if (args.length != 3) {
            MessageHandler.sendMessage(player, "calc_usage");
            return true;
        }

        // Standard calculator logic
        double num1, num2;
        String operator = args[1];
        double result;

        try {
            num1 = Double.parseDouble(args[0]);
            num2 = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            MessageHandler.sendMessage(player, "calc_invalid_number");
            return true;
        }

        switch (operator) {
            case "+":
                result = num1 + num2;
                break;
            case "-":
                result = num1 - num2;
                break;
            case "*":
            case "x":
                result = num1 * num2;
                break;
            case "/":
                if (num2 == 0) {
                    MessageHandler.sendMessage(player, "calc_divide_by_zero");
                    return true;
                }
                result = num1 / num2;
                break;
            default:
                MessageHandler.sendMessage(player, "calc_invalid_operator");
                return true;
        }

        MessageHandler.sendMessage(player, "calc_result", "{result}", String.valueOf(result));
        return true;
    }
}
