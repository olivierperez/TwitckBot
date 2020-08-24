package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.utils.tryToInt
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PointsCommands(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val bank: PointsBank,
    private val message: Messages
) {

    private val logger: Logger = LoggerFactory.getLogger(PointsCommands::class.java)

    fun reactTo(command: Command, messageEvent: MessageEvent, bot: TwitckBot) {
        when (command.tag) {
            // !points_add Pipiks_ 13000
            "!points_add" -> handleAddCommand(command)
            // !points_transfer idontwantgiftsub 152
            "!points_transfer" -> handleTransferCommand(command, messageEvent, bot)
            // !points_info
            "!points_info" -> handleInfoCommand(messageEvent, bot)
        }
    }

    private fun handleAddCommand(command: Command) {
        if (command.badges.none { badge -> badge in privilegedBadges }) return

        if (command.options.size == 2) {
            val login = command.options[0].toLowerCase()
            val points = command.options[1].tryToInt()
            logger.debug("[Command:AddPoints] ${command.login} try to add $points to $login")

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
            logger.debug("[Command:AddPoints] ${command.login} try to transfer $points to $toLogin")

            // TODO OPZ Remplacer messageEvent par command
            if (toLogin == messageEvent.login) return

            points?.let {
                val transferSucceeded = bank.transferPoints(messageEvent.login, toLogin, points)
                val msg = if (transferSucceeded) {
                    // TODO Faire ce message en whisper, directement à l'emetteur (si possible)
                    message.pointsTransferred
                        .replace("#FROM#", messageEvent.login)
                        .replace("#TO#", toLogin)
                } else {
                    message.notEnoughPoints
                        .replace("#FROM#", messageEvent.login)
                        .replace("#TO#", toLogin)
                }
                bot.send(channel, msg)
            }
        }
    }

    // TODO OPZ Remplacer messageEvent par command
    private fun handleInfoCommand(messageEvent: MessageEvent, bot: TwitckBot) {
        val points = bank.getPoints(messageEvent.login)
        logger.debug("[Command:AddPoints] ${messageEvent.login} requested points info ($points)")
        val msg = if (points == 0) {
            message.viewerHasNoPoints
                .replace("#USER#", messageEvent.login)
                .replace("#POINTS#", points.toString())
        } else {
            message.viewerHasPoints
                .replace("#USER#", messageEvent.login)
                .replace("#POINTS#", points.toString())
        }
        bot.send(channel, msg)
    }
}