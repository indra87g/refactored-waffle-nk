package com.indra87g.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.Player;
import com.indra87g.utils.MessageHandler;

public abstract class BaseCommand extends Command {

    public BaseCommand(String name, String description, String usage, String permission) {
        super(name);
        this.setDescription(description);
        this.setUsage(usage);
        this.setPermission(permission);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            MessageHandler.sendMessage(sender, "permission_denied");
            return false;
        }

        if (!(sender instanceof Player)) {
            MessageHandler.sendMessage(sender, "player_only_command");
            return false;
        }

        Player player = (Player) sender;

        if (!validateArgs(args, player)) {
            // The validateArgs method is responsible for sending the usage message.
            return false;
        }

        try {
            return handleCommand(player, args);
        } catch (Exception e) {
            MessageHandler.sendMessage(sender, "generic_error");
            e.printStackTrace();
            return false;
        }
    }

    protected boolean validateArgs(String[] args, Player player) {
        return true;
    }
  
    protected abstract boolean handleCommand(Player player, String[] args);
}
