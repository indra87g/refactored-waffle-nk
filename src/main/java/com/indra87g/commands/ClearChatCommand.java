package com.indra87g.commands;

import cn.nukkit.Player;
import com.indra87g.utils.MessageHandler;

public class ClearChatCommand extends BaseCommand {

    public ClearChatCommand() {
        super("clearchat", "Clear your chat", "/clearchat [amount]", "waffle.clear.chat");
    }

    @Override
    protected boolean validateArgs(String[] args, Player player) {
        if (args.length > 1) {
            MessageHandler.sendMessage(player, "clearchat_too_many_args");
            MessageHandler.sendMessage(player, "invalid_usage", "{usage}", this.getUsage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        int amount = 100; // default

        if (args.length == 1) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount < 100 || amount > 500) {
                    MessageHandler.sendMessage(player, "clearchat_invalid_amount_range");
                    return false;
                }
            } catch (NumberFormatException e) {
                MessageHandler.sendMessage(player, "clearchat_invalid_amount_type");
                return false;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            sb.append("\n");
        }
        player.sendMessage(sb.toString());

        MessageHandler.sendMessage(player, "clearchat_success", "{amount}", String.valueOf(amount));
        return true;
    }
}
