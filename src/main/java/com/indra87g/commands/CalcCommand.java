package com.indra87g.commands;

import cn.nukkit.Player;

public class CalcCommand extends BaseCommand {

    public CalcCommand() {
        super("calc", "Do a mathematical calculation", "/calc <num1> <operator> <num2>", "waffle.calc");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length != 3) {
            player.sendMessage("§cUsage: /calc <num1> <operator> <num2>");
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        double num1, num2;
        String operator = args[1];
        double result;

        try {
            num1 = Double.parseDouble(args[0]);
            num2 = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number!");
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
                    player.sendMessage("§cCannot divide by zero!");
                    return true;
                }
                result = num1 / num2;
                break;
            default:
                player.sendMessage("§cUnknown operator! Please use +, -, *, or /");
                return true;
        }

        player.sendMessage("§aResult: §e" + result);
        return true;
    }
}
