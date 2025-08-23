package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.plugin.Plugin;
import me.onebone.economyapi.EconomyAPI;

import java.util.HashMap;
import java.util.UUID;

public class RoamCommand extends BaseCommand {
    private final Plugin plugin;
    private final HashMap<UUID, Integer> roamMinutes = new HashMap<>();
    private final HashMap<UUID, NukkitRunnable> roamTasks = new HashMap<>();

    public RoamCommand(Plugin plugin) {
        super("roam", "Enter roaming mode (spectator)", "/roam [cancel|trial]", "waffle.roam");
        this.plugin = plugin;
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            if (roamTasks.containsKey(player.getUniqueId())) {
                player.sendMessage("§cYou are entering roam mode! Type /roam cancel to exit.");
                return true;
            }

            player.setGamemode(Player.SPECTATOR);
            player.sendMessage("§aRoaming charges: 150 money/minute.");

            roamMinutes.put(player.getUniqueId(), 0);

            NukkitRunnable task = new NukkitRunnable() {
                @Override
                public void run() {
                    double balance = EconomyAPI.getInstance().myMoney(player);
                    if (balance < 150) {
                        player.sendMessage("§cOut of money! Roaming cancelled.");
                        cancelRoam(player);
                        return;
                    }

                    EconomyAPI.getInstance().reduceMoney(player, 150);
                    roamMinutes.put(player.getUniqueId(), roamMinutes.get(player.getUniqueId()) + 1);
                    player.sendMessage("§e150 money deducted for roaming. Total minutes: " + roamMinutes.get(player.getUniqueId()));
                }
            };
            task.runTaskTimer(plugin, 20 * 60, 20 * 60);
            roamTasks.put(player.getUniqueId(), task);
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            if (!roamTasks.containsKey(player.getUniqueId())) {
                player.sendMessage("§cYou are not roaming!");
                return true;
            }
            int minutes = roamMinutes.getOrDefault(player.getUniqueId(), 0);
            cancelRoam(player);
            player.sendMessage("§aRoam ender! Total money spend: " + (minutes * 150));
            return true;
        }

        if (args[0].equalsIgnoreCase("trial")) {
            player.setGamemode(Player.CREATIVE);
            player.sendMessage("§aTrial mode started! You can use creative mode for 1 minute.");
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                if (player.isOnline() && player.getGamemode() == Player.CREATIVE) {
                    player.setGamemode(Player.SURVIVAL);
                    player.sendMessage("§eTrial mode ended!");
                }
            }, 20 * 60);
            return true;
        }

        player.sendMessage("§cUsage: /roam [cancel|trial]");
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
}
