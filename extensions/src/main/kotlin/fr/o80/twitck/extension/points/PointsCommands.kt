package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.sanitizeLogin
import fr.o80.twitck.lib.utils.tryToInt
import java.time.Duration

class PointsCommands(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val bank: PointsBank,
    private val message: Messages,
    private val extensionProvider: ExtensionProvider,
    private val logger: Logger
) {

    fun interceptCommandEvent(commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            // !points_add Pipiks_ 13000
            "!points_add" -> handleAddCommand(commandEvent)
            // !points_give idontwantgiftsub 152
            "!points_give" -> handleGiveCommand(commandEvent)
            // !points_info
            "!points_info" -> handleInfoCommand(commandEvent)
        }

        return commandEvent
    }

    private fun handleAddCommand(commandEvent: CommandEvent) {
        if (commandEvent.badges.none { badge -> badge in privilegedBadges }) return

        val command = commandEvent.command
        if (command.options.size == 2) {
            val login = command.options[0].sanitizeLogin()
            val points = command.options[1].tryToInt()
            logger.command(command, "${commandEvent.login} try to add $points to $login")

            points?.let {
                bank.addPoints(login, points)
            }
        }
    }

    // TODO OPZ Vérifier si le viewer de destination existe (toto VS toot)
    private fun handleGiveCommand(commandEvent: CommandEvent) {
        val command = commandEvent.command
        if (command.options.size == 2) {
            val fromLogin = commandEvent.login
            val toLogin = command.options[0].sanitizeLogin()
            val points = command.options[1].tryToInt()
            logger.command(command, "${commandEvent.login} try to transfer $points to $toLogin")

            if (toLogin == fromLogin) return
            if (points == null) return

            val transferSucceeded = bank.transferPoints(fromLogin, toLogin, points)
            val msg = if (transferSucceeded) {
                // TODO Faire ce message en whisper, directement à l'emetteur (si possible)
                message.pointsTransferred
                    .replace("#FROM#", fromLogin)
                    .replace("#TO#", toLogin)
            } else {
                message.notEnoughPoints
                    .replace("#FROM#", fromLogin)
                    .replace("#TO#", toLogin)
            }

            extensionProvider.alertOverlay(msg, Duration.ofSeconds(5))
        }
    }

    private fun handleInfoCommand(commandEvent: CommandEvent) {
        val command = commandEvent.command
        val login = commandEvent.login
        val points = bank.getPoints(login)

        logger.command(command, "$login requested points info ($points)")

        val msg = if (points == 0) {
            message.viewerHasNoPoints
                .replace("#USER#", login)
                .replace("#POINTS#", points.toString())
        } else {
            message.viewerHasPoints
                .replace("#USER#", login)
                .replace("#POINTS#", points.toString())
        }

        extensionProvider.alertOverlay(msg, Duration.ofSeconds(5))
    }
}

private fun ExtensionProvider.alertOverlay(msg: String, duration: Duration) {
    this.forEach(Overlay::class) { overlay ->
        overlay.alert(msg, duration)
    }
}
