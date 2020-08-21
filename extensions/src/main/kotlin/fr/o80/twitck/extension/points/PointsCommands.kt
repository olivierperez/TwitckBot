package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.utils.tryToInt

class PointsCommands(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val bank: PointsBank
) {

    fun reactTo(command: Command, messageEvent: MessageEvent, bot: TwitckBot) {
        when (command.tag) {
            "!points_add" -> handleAddCommand(command) // !points_add Pipiks_ 13000
            "!points_transfer" -> handleTransferCommand(command, messageEvent, bot) // !points_transfer idontwantgiftsub 152
            "!points_info" -> handleInfoCommand(messageEvent, bot) // !points_info
        }
    }

    private fun handleAddCommand(command: Command) {
        if (command.badges.none { badge -> badge in privilegedBadges }) return

        if (command.options.size == 2) {
            val login = command.options[0].toLowerCase()
            val points = command.options[1].tryToInt()

            points?.let {
                bank.addPoints(login, points)
            }
        }
    }

    private fun handleTransferCommand(
        command: Command,
        messageEvent: MessageEvent,
        bot: TwitckBot
    ) {
        if (command.options.size == 2) {
            val toLogin = command.options[0].toLowerCase()
            val points = command.options[1].tryToInt()

            if (toLogin == messageEvent.login) return

            points?.let {
                val transferSucceeded = bank.transferPoints(messageEvent.login, toLogin, points)
                if (transferSucceeded) {
                    // TODO Faire ce message en whisper, directement à l'emetteur (si possible)
                    bot.send(channel, "Points transferés de ${messageEvent.login} à $toLogin")
                } else {
                    bot.send(channel, "Les huissiers sont en route vers ${messageEvent.login}")
                }
            }
        }
    }

    private fun handleInfoCommand(messageEvent: MessageEvent, bot: TwitckBot) {
        val points = bank.getPoints(messageEvent.login)
        if (points == 0) {
            bot.send(channel, "${messageEvent.login} n'a pas de point")
        } else {
            bot.send(channel, "${messageEvent.login} a $points point(s)")
        }
    }
}