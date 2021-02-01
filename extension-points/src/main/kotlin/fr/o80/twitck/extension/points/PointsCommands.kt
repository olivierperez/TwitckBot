package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.sanitizeLogin
import fr.o80.twitck.lib.utils.tryToInt

const val POINTS_ADD_COMMAND = "!points_add"
const val POINTS_GIVE_COMMAND = "!points_give"
const val POINTS_COMMAND = "!points"
const val POINTS_COMMAND_2 = "!points_info"

class PointsCommands(
    private val channel: String,
    private val privilegedBadges: Collection<Badge>,
    private val message: PointsI18n,
    private val bank: PointsBank,
    private val logger: Logger,
    private val storage: StorageExtension
) {

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            // !points_add Pipiks_ 13000
            POINTS_ADD_COMMAND -> handleAddCommand(commandEvent)
            // !points_give idontwantgiftsub 152
            POINTS_GIVE_COMMAND -> handleGiveCommand(messenger, commandEvent)
            // !points
            POINTS_COMMAND, POINTS_COMMAND_2 -> handleInfoCommand(messenger, commandEvent)
        }

        return commandEvent
    }

    private fun handleAddCommand(commandEvent: CommandEvent) {
        if (commandEvent.viewer hasNoPrivilegesOf privilegedBadges) return

        val command = commandEvent.command
        if (command.options.size == 2) {
            val login = command.options[0].sanitizeLogin()
            val points = command.options[1].tryToInt()
            logger.command(command, "${commandEvent.viewer.displayName} try to add $points to $login")

            points?.let {
                bank.addPoints(login, points)
            }
        }
    }

    private fun handleGiveCommand(messenger: Messenger, commandEvent: CommandEvent) {
        val command = commandEvent.command
        if (command.options.size == 2) {
            val fromLogin = commandEvent.viewer.login
            val toLogin = command.options[0].sanitizeLogin()
            val points = command.options[1].tryToInt()
            logger.command(command, "${commandEvent.viewer.displayName} try to transfer $points to $toLogin")

            if (toLogin == fromLogin) return
            if (points == null) return
            if (!storage.hasUserInfo(toLogin)) {
                messenger.sendImmediately(commandEvent.channel, message.destinationViewerDoesNotExist)
                return
            }

            val transferSucceeded = bank.transferPoints(fromLogin, toLogin, points)
            val msg = if (transferSucceeded) {
                message.pointsTransferred
                    .replace("#FROM#", commandEvent.viewer.displayName)
                    .replace("#TO#", toLogin)
            } else {
                message.notEnoughPoints
                    .replace("#FROM#", commandEvent.viewer.displayName)
                    .replace("#TO#", toLogin)
            }

            messenger.whisper(commandEvent.channel, commandEvent.viewer.login, msg)
        }
    }

    private fun handleInfoCommand(messenger: Messenger, commandEvent: CommandEvent) {
        val command = commandEvent.command
        val points = bank.getPoints(commandEvent.viewer.login)

        logger.command(command, "${commandEvent.viewer.displayName} requested points info ($points)")

        val msg = if (points == 0) {
            message.viewerHasNoPoints
                .replace("#USER#", commandEvent.viewer.displayName)
                .replace("#POINTS#", points.toString())
        } else {
            message.viewerHasPoints
                .replace("#USER#", commandEvent.viewer.displayName)
                .replace("#POINTS#", points.toString())
        }

        messenger.sendImmediately(commandEvent.channel, msg)
    }
}
