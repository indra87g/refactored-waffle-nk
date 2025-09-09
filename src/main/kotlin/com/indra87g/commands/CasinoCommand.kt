package com.indra87g.commands

import cn.nukkit.Player
import com.indra87g.utils.MessageHandler
import me.onebone.economyapi.EconomyAPI
import java.util.Random
import kotlin.random.asKotlinRandom

class CasinoCommand : BaseCommand("casino", "Play casino game", "/casino <coinflip|slot|dice> ...", "waffle.casino") {

    private val random = Random().asKotlinRandom()
    private val economyAPI = EconomyAPI.getInstance()

    override fun validateArgs(args: Array<String>, player: Player): Boolean {
        if (args.isEmpty()) {
            MessageHandler.sendMessage(player, "casino_usage")
            return false
        }
        return true
    }

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        val game = args[0].lowercase()

        return when (game) {
            "coinflip" -> handleCoinFlip(player, args)
            "slot" -> handleSlot(player, args)
            "dice" -> handleDice(player, args)
            else -> {
                MessageHandler.sendMessage(player, "casino_game_not_found")
                false
            }
        }
    }

    private fun handleBet(player: Player, amountStr: String): Double {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            MessageHandler.sendMessage(player, "invalid_bet_amount")
            return -1.0
        }

        if (economyAPI.myMoney(player) < amount) {
            MessageHandler.sendMessage(player, "not_enough_money")
            return -1.0
        }
        return amount
    }

    private fun handleCoinFlip(player: Player, args: Array<String>): Boolean {
        if (args.size < 3) {
            MessageHandler.sendMessage(player, "coinflip_usage")
            return false
        }

        val choice = args[1].lowercase()
        if (choice != "heads" && choice != "tails") {
            MessageHandler.sendMessage(player, "coinflip_invalid_choice")
            return false
        }

        val amount = handleBet(player, args[2])
        if (amount < 0) {
            return false
        }

        val result = if (random.nextBoolean()) "heads" else "tails"
        if (choice == result) {
            economyAPI.addMoney(player, amount)
            MessageHandler.sendMessage(player, "coinflip_win", "{result}", result, "{amount}", amount.toString())
        } else {
            economyAPI.reduceMoney(player, amount)
            MessageHandler.sendMessage(player, "coinflip_lose", "{result}", result, "{amount}", amount.toString())
        }
        return true
    }

    private fun handleSlot(player: Player, args: Array<String>): Boolean {
        if (args.size < 2) {
            MessageHandler.sendMessage(player, "slot_usage")
            return false
        }

        val amount = handleBet(player, args[1])
        if (amount < 0) {
            return false
        }

        val symbols = arrayOf("○", "□", "♡", "◇")
        val s1 = symbols.random(random)
        val s2 = symbols.random(random)
        val s3 = symbols.random(random)

        MessageHandler.sendMessage(player, "slot_result", "{s1}", s1, "{s2}", s2, "{s3}", s3)

        when {
            s1 == s2 && s2 == s3 -> {
                val prize = amount * 5
                economyAPI.addMoney(player, prize)
                MessageHandler.sendMessage(player, "slot_win_triple", "{amount}", prize.toString())
            }
            s1 == s2 || s2 == s3 || s1 == s3 -> {
                val prize = amount * 2
                economyAPI.addMoney(player, prize)
                MessageHandler.sendMessage(player, "slot_win_double", "{amount}", prize.toString())
            }
            else -> {
                economyAPI.reduceMoney(player, amount)
                MessageHandler.sendMessage(player, "slot_lose", "{amount}", amount.toString())
            }
        }
        return true
    }

    private fun handleDice(player: Player, args: Array<String>): Boolean {
        if (args.size < 3) {
            MessageHandler.sendMessage(player, "dice_usage")
            return false
        }

        val guess = args[1].toIntOrNull()
        if (guess == null || guess !in 1..6) {
            MessageHandler.sendMessage(player, "dice_invalid_guess")
            return false
        }

        val amount = handleBet(player, args[2])
        if (amount < 0) {
            return false
        }

        val roll = random.nextInt(1, 7)
        MessageHandler.sendMessage(player, "dice_result", "{roll}", roll.toString())

        if (roll == guess) {
            val prize = amount * 6
            economyAPI.addMoney(player, prize)
            MessageHandler.sendMessage(player, "dice_win", "{amount}", prize.toString())
        } else {
            economyAPI.reduceMoney(player, amount)
            MessageHandler.sendMessage(player, "dice_lose", "{amount}", amount.toString())
        }
        return true
    }
}
