package com.indra87g.commands;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.EmotePacket;

public class PlayAnimationCommand extends BaseCommand {

    public PlayAnimationCommand() {
        super("playanimation", "Play a custom animation", "/playanimation <animation>", "waffle.playanimation");
    }

    @Override
    protected boolean handleCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /playanimation <animation>");
            return true;
        }

        String anim = args[0];

        EmotePacket packet = new EmotePacket();
        packet.runtimeEntityId = player.getId();
        packet.emoteId = anim;
        packet.flags = EmotePacket.FLAG_SERVER_SIDE;
      
        player.dataPacket(packet);
        player.sendMessage("§aPlaying animation: §e" + anim);
        return true;
    }
}
