package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.plugin.Plugin;
import com.indra87g.utils.MessageHandler;
import me.onebone.economyapi.EconomyAPI;

import java.util.HashMap;
import java.util.UUID;

public class RoamCommand extends BaseCommand {
    private static RoamCommand instance;
    private final Plugin plugin;
    private final HashMap<UUID, Integer> roamMinutes = new HashMap<>();
    private final HashMap<UUID, NukkitRunnable> roamTasks = new HashMap<>();
    private final EconomyAPI economyAPI = EconomyAPI.getInstance();

    public RoamCommand(Plugin plugin) {
        super("roam", "Enter roaming mode (spectator)", "/roam [cancel|trial]", "waffle.roam");
        this.plugin = plugin;
        instance = this;
    }

    public static RoamCommand getInstance() {
        return instance;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            if (roamTasks.containsKey(player.getUniqueId())) {
                MessageHandler.sendMessage(player, "roam_already_roaming");
                return true;
            }

            player.setGamemode(Player.SPECTATOR);
            MessageHandler.sendMessage(player, "roam_start");

            roamMinutes.put(player.getUniqueId(), 0);

            NukkitRunnable task = new NukkitRunnable() {
                @Override
                public void run() {
                    if (economyAPI.myMoney(player) < 150) {
                        MessageHandler.sendMessage(player, "roam_no_money");
                        cancelRoam(player);
                        return;
                    }

                    economyAPI.reduceMoney(player, 150);
                    int currentMinutes = roamMinutes.getOrDefault(player.getUniqueId(), 0) + 1;
                    roamMinutes.put(player.getUniqueId(), currentMinutes);
                    MessageHandler.sendMessage(player, "roam_fee_deducted", "{minutes}", String.valueOf(currentMinutes));
                }
            };
            task.runTaskTimer(plugin, 20 * 60, 20 * 60);
            roamTasks.put(player.getUniqueId(), task);
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            if (!roamTasks.containsKey(player.getUniqueId())) {
                MessageHandler.sendMessage(player, "roam_not_roaming");
                return true;
            }
            int minutes = roamMinutes.getOrDefault(player.getUniqueId(), 0);
            cancelRoam(player);
            MessageHandler.sendMessage(player, "roam_ended", "{amount}", String.valueOf(minutes * 150));
            return true;
        }

        if (args[0].equalsIgnoreCase("trial")) {
            player.setGamemode(Player.SPECTATOR);
            MessageHandler.sendMessage(player, "roam_trial_start");
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                if (player.isOnline() && player.getGamemode() == Player.SPECTATOR) {
                    player.setGamemode(Player.SURVIVAL);
                    MessageHandler.sendMessage(player, "roam_trial_ended");
                }
            }, 20 * 60);
            return true;
        }

        MessageHandler.sendMessage(player, "roam_usage");
        return true;
    }

    private void cancelRoam(Player player) {
        UUID id = player.getUniqueId();
        if (roamTasks.containsKey(id)) {
            roamTasks.get(id).cancel();
            roamTasks.remove(id);
            roamMinutes.remove(id);
        }
        if (player.getGamemode() == Player.SPECTATOR) {
            player.setGamemode(Player.SURVIVAL);
        }
    }
    
    public void forceCancel(Player player) {
        cancelRoam(player);
    }

}
