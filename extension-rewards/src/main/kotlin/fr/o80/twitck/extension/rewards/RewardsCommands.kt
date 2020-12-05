package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.time.TimeChecker
import java.time.Duration

class RewardsCommands(
    private val channel: String,
    private val extensionProvider: ExtensionProvider,
    private val claimTimeChecker : TimeChecker,
    private val claimedPoints: Int,
    private val messages: Messages
) {

    fun interceptCommandEvent(commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            "!claim" -> claim(commandEvent.viewer)
        }

        return commandEvent
    }

    private fun claim(viewer: Viewer) {
        if (claimedPoints == 0) return

        claimTimeChecker.executeIfNotCooldown(viewer.login) {
            val ownedPoints = extensionProvider.provide(PointsManager::class)
                .filter { it.channel == channel }
                .onEach { pointsManager ->
                    pointsManager.addPoints(viewer.login, claimedPoints)
                }
                .sumBy { pointsManager -> pointsManager.getPoints(viewer.login) }

            val message = messages.viewerJustClaimed
                .replace("#USER#", viewer.displayName)
                .replace("#NEW_POINTS#", claimedPoints.toString())
                .replace("#OWNED_POINTS#", ownedPoints.toString())

            extensionProvider.first(OverlayExtension::class)
                .alert(message, Duration.ofSeconds(10))
            extensionProvider.first(SoundExtension::class)
                .playCoin()
        }
    }

}
