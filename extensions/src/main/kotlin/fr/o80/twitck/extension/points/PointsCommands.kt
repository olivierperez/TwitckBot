package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.tryToInt
import java.time.Duration

class PointsCommands(
    private val privilegedBadges: Array<out Badge>,
    private val bank: PointsBank,
    private val message: Messages,
    private val extensionProvider: ExtensionProvider,
    private val logger: Logger
) {

    fun reactTo(command: Command) {
        when (command.tag) {
            // !points_add Pipiks_ 13000
            "!points_add" -> handleAddCommand(command)
            // !points_transfer idontwantgiftsub 152
            "!points_transfer" -> handleTransferCommand(command)
            // !points_info
            "!points_info" -> handleInfoCommand(command)
        }
    }

    private fun handleAddCommand(command: Command) {
        if (command.badges.none { badge -> badge in privilegedBadges }) return

        if (command.options.size == 2) {
            val login = command.options[0].toLowerCase()
            val points = command.options[1].tryToInt()
            logger.command(command, "${command.login} try to add $points to $login")

            points?.let {
                bank.addPoints(login, points)
            }
        }
    }

    private fun handleTransferCommand(
        command: Command
    ) {
        if (command.options.size == 2) {
            val fromLogin = command.login
            val toLogin = command.options[0].toLowerCase()
            val points = command.options[1].tryToInt()
            logger.command(command, "${command.login} try to transfer $points to $toLogin")

            if (toLogin == fromLogin) return

            points?.let {
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
    }

    private fun handleInfoCommand(command: Command) {
        val login = command.login
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
    this.provide(Overlay::class).forEach { overlay ->
        overlay.alert(msg, duration)
    }
}
