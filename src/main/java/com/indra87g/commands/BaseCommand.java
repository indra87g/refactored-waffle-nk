package com.indra87g.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.Player;

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
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command only for player!");
            return false;
        }

        Player player = (Player) sender;

        if (!validateArgs(args, player)) {
            return false;
        }

        try {
            return handleCommand(player, args);
        } catch (Exception e) {
            player.sendMessage("§cError on running command.");
            e.printStackTrace();
            return false;
        }
    }

    protected boolean validateArgs(String[] args, Player player) {
        return true;
    }
  
    protected abstract boolean handleCommand(Player player, String[] args);
}
