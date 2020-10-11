package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.time.TimeChecker

class RewardsCommands(
    private val channel: String,
    private val extensionProvider: ExtensionProvider,
    private val claimTimeChecker : TimeChecker,
    private val claimedPoints: Int,
    private val messages: Messages
) {

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            "!claim" -> claim(messenger, commandEvent.login)
        }

        return commandEvent
    }

    private fun claim(messenger: Messenger, login: String) {
        if (claimedPoints == 0) return

        claimTimeChecker.executeIfNotCooldown(login) {
            val ownedPoints = extensionProvider.provide(PointsManager::class)
                .filter { it.channel == channel }
                .onEach { pointsManager ->
                    pointsManager.addPoints(login, claimedPoints)
                }
                .sumBy { pointsManager -> pointsManager.getPoints(login) }

            val message = messages.viewerJustClaimed
                .replace("#USER#", login)
                .replace("#NEW_POINTS#", claimedPoints.toString())
                .replace("#OWNED_POINTS#", ownedPoints.toString())

            messenger.sendImmediately(channel, message)
        }
    }

}
