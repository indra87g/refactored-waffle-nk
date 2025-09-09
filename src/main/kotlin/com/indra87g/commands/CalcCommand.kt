package com.indra87g.commands

import cn.nukkit.Player
import com.indra87g.utils.MessageHandler

class CalcCommand : BaseCommand("calc", "Do a mathematical calculation", "/calc <num1> <operator> <num2>", "waffle.calc") {

    override fun validateArgs(args: Array<String>, player: Player): Boolean {
        if (args.size != 3) {
            MessageHandler.sendMessage(player, "calc_usage")
            return false
        }
        return true
    }

    override fun handleCommand(player: Player, args: Array<String>): Boolean {
        val num1: Double
        val num2: Double
        val operator = args[1]
        val result: Double

        try {
            num1 = args[0].toDouble()
            num2 = args[2].toDouble()
        } catch (e: NumberFormatException) {
            MessageHandler.sendMessage(player, "calc_invalid_number")
            return true
        }

        result = when (operator) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*", "x" -> num1 * num2
            "/" -> {
                if (num2 == 0.0) {
                    MessageHandler.sendMessage(player, "calc_divide_by_zero")
                    return true
                }
                num1 / num2
            }
            else -> {
                MessageHandler.sendMessage(player, "calc_invalid_operator")
                return true
            }
        }

        MessageHandler.sendMessage(player, "calc_result", "{result}", result.toString())
        return true
    }
}
