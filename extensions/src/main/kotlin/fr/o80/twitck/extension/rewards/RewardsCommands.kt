package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.utils.tryToLong

class RewardsCommands(
    private val channel: String,
    private val extensionProvider: ExtensionProvider,
    private val intervalBetweenTwoClaims: Long,
    private val claimedPoints: Int,
    private val messages: Messages
) {

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = Rewards::class.java.name


    fun interceptCommandEvent(bot: TwitckBot, commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            "!claim" -> claim(bot, commandEvent.login)
        }

        return commandEvent
    }

    private fun claim(bot: TwitckBot, login: String) {
        if (alreadyClaimed(login)) {
            return
        }

        rememberLastClaimIsNow(login)

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
        bot.send(channel, message)
    }

    private fun alreadyClaimed(login: String): Boolean {
        val lastClaimedAt = storage.getUserInfo(login, namespace, "lastClaimedAt").tryToLong()
        return lastClaimedAt != null && lastClaimedAt + intervalBetweenTwoClaims > System.currentTimeMillis()
    }

    private fun rememberLastClaimIsNow(login: String) {
        storage.putUserInfo(login, namespace, "lastClaimedAt", System.currentTimeMillis().toString())
    }

}
