package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class CalcCommand extends BaseCommand {

    public CalcCommand() {
        super("calc", "Do a mathematical calculations ", "/calc <num1> <operator> <num2>", "waffle.calc");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage("§cUsage: /calc <num1> <operator> <num22>");
            return true;
        }

        double num1, num2;
        String operator = args[1];
        double result;

        try {
            num1 = Double.parseDouble(args[0]);
            num2 = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number!");
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
                    sender.sendMessage("§cCannot divide by zero!");
                    return true;
                }
                result = num1 / num2;
                break;
            default:
                sender.sendMessage("§cUnknown operator! Please use +, -, *, or /");
                return true;
        }

        sender.sendMessage("§aResult: §e" + result);
        return true;
    }
}
