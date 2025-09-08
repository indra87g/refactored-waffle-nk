package com.indra87g.utils;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageHandler {

    private static Config messageConfig;
    private static final Map<String, String> messages = new HashMap<>();
    private static String prefix = "";

    public static void loadMessages(Main plugin) {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messageConfig = new Config(messagesFile, Config.YAML);
        prefix = TextFormat.colorize(messageConfig.getString("prefix", "&7[&eWaffle&7] &r"));

        for (String key : messageConfig.getKeys(false)) {
            if (!key.equals("prefix")) {
                messages.put(key, messageConfig.getString(key));
            }
        }
    }

    public static void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = messages.getOrDefault(key, "&cMessage not found: " + key);
        message = replacePlaceholders(message, replacements);
        sender.sendMessage(prefix + TextFormat.colorize(message));
    }

    public static void sendDirectMessage(CommandSender sender, String message) {
        sender.sendMessage(TextFormat.colorize(message));
    }

    public static String getMessage(String key) {
        return messages.getOrDefault(key, "&cMessage not found: " + key);
    }

    public static String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        return replacePlaceholders(message, replacements);
    }

    private static String replacePlaceholders(String message, String... replacements) {
        if (replacements.length % 2 != 0) {
            return message; // Invalid replacements
        }
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }
}
