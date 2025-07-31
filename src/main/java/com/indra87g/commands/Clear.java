package com.indra87g.commands;

import cn.nukkit.Player;

public class ClearCommand extends BaseCommand {

    public ClearCommand() {
        super("clear", "Clear your chat", "/clear [amount]", "waffle.clear");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length > 1) {
            player.sendMessage("§cToo many arguments.");
            player.sendMessage("§eUsage: " + this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        int jumlah = 100; // default

        if (args.length == 1) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount <= 0 || amount > 500) {
                    player.sendMessage("§cAmount must be between 1 - 500.");
                    return false;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cAmount must be integer.");
                return false;
            }
        }

        for (int i = 0; i < amount; i++) {
            player.sendMessage(""); 
        }

        player.sendMessage("§aChat cleared successfully! (" + amount + " lines).");
        return true;
    }
}
