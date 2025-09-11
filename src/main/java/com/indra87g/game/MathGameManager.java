package com.indra87g.game;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.indra87g.Main;
import me.onebone.economyapi.EconomyAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MathGameManager {

    private final Main plugin;
    private final Config config;
    private final Map<UUID, GameState> activeGames = new HashMap<>();
    private final Random random = new Random();

    public MathGameManager(Main plugin) {
        this.plugin = plugin;
        this.config = new Config(plugin.getDataFolder() + "/math_game.yml", Config.YAML);
    }

    public boolean isInGame(Player player) {
        return activeGames.containsKey(player.getUniqueId());
    }

    public void startGame(Player player) {
        if (isInGame(player)) {
            sendMessage(player, "messages.already_in_game", null);
            return;
        }

        GameState state = new GameState();
        activeGames.put(player.getUniqueId(), state);

        String[] startMessage = getMessage("messages.game_start", "{questions_total}", String.valueOf(config.getInt("game.questions_total"))).split("\n");
        for(String line : startMessage) {
            player.sendMessage(line);
        }

        generateQuestion(player);
    }

    public void endGame(Player player) {
        GameState state = activeGames.remove(player.getUniqueId());
        if (state != null) {
            String[] endMessage = getMessage("messages.game_over",
                "{correct_total}", String.valueOf(state.correctAnswers),
                "{questions_total}", String.valueOf(config.getInt("game.questions_total")),
                "{final_reward}", String.valueOf(state.score)
            ).split("\n");
            for(String line : endMessage) {
                player.sendMessage(line);
            }
            EconomyAPI.getInstance().addMoney(player, state.score);
        }
    }

    public void handleAnswer(Player player, String message) {
        GameState state = activeGames.get(player.getUniqueId());
        if (state == null) return;

        try {
            int answer = Integer.parseInt(message);
            if (answer == state.expectedAnswer) {
                // Correct
                state.correctAnswers++;
                state.score += config.getDouble("rewards.base_correct", 50);
                state.score += config.getDouble("rewards.bonus." + state.lastOperation, 0);
                sendMessage(player, "messages.correct_answer", "{score}", String.valueOf(state.score));
            } else {
                // Incorrect
                sendMessage(player, "messages.incorrect_answer",
                    "{correct_answer}", String.valueOf(state.expectedAnswer),
                    "{score}", String.valueOf(state.score)
                );
            }

            state.currentQuestionNumber++;

            if (state.currentQuestionNumber > config.getInt("game.questions_total", 5)) {
                endGame(player);
            } else {
                generateQuestion(player);
            }

        } catch (NumberFormatException e) {
            sendMessage(player, "messages.invalid_answer", null);
        }
    }

    private void generateQuestion(Player player) {
        GameState state = activeGames.get(player.getUniqueId());
        if (state == null) return;

        int operation = random.nextInt(4); // 0:+, 1:-, 2:*, 3:/
        int num1 = 0, num2 = 0;
        String opSymbol = "";

        switch (operation) {
            case 0: // Addition
                num1 = randInt("addition.min", "addition.max");
                num2 = randInt("addition.min", "addition.max");
                state.expectedAnswer = num1 + num2;
                opSymbol = "+";
                state.lastOperation = "addition";
                break;
            case 1: // Subtraction
                num1 = randInt("subtraction.min", "subtraction.max");
                num2 = randInt("subtraction.min", "subtraction.max");
                // Ensure result is not negative
                if (num2 > num1) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                state.expectedAnswer = num1 - num2;
                opSymbol = "-";
                state.lastOperation = "subtraction";
                break;
            case 2: // Multiplication
                num1 = randInt("multiplication.min", "multiplication.max");
                num2 = randInt("multiplication.min", "multiplication.max");
                state.expectedAnswer = num1 * num2;
                opSymbol = "*";
                state.lastOperation = "multiplication";
                break;
            case 3: // Division
                int divisor = randInt("division.min_divisor", "division.max_divisor");
                int multiplier = randInt("division.min_multiplier", "division.max_multiplier");
                num1 = divisor * multiplier;
                num2 = divisor;
                state.expectedAnswer = multiplier;
                opSymbol = "/";
                state.lastOperation = "division";
                break;
        }

        sendMessage(player, "messages.question",
            "{current_question}", String.valueOf(state.currentQuestionNumber),
            "{questions_total}", String.valueOf(config.getInt("game.questions_total")),
            "{num1}", String.valueOf(num1),
            "{operator}", opSymbol,
            "{num2}", String.valueOf(num2)
        );
    }

    private int randInt(String minKey, String maxKey) {
        int min = config.getInt("game." + minKey);
        int max = config.getInt("game." + maxKey);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void sendMessage(Player player, String key, String... replacements) {
        String message = getMessage(key, replacements);
        player.sendMessage(message);
    }

    private String getMessage(String key, String... replacements) {
        String message = config.getSection("messages").getString(key, "&cMessage not found: " + key);
        message = TextFormat.colorize(config.getString("messages.game_prefix", "") + message);
        if (replacements != null && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }

    private static class GameState {
        int currentQuestionNumber = 1;
        int correctAnswers = 0;
        int score = 0;
        int expectedAnswer;
        String lastOperation;
    }
}
