package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.utils.tryToLong
import java.util.concurrent.TimeUnit

// TODO OPZ Extraire la partie gestion des commandes
class Rewards(
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

    private fun onInstallationFinished() {
        extensionProvider.forEach(Overlay::class) { overlay ->
            overlay.provideInformation(
                namespace,
                listOf("Vous pouvez !claim des ${messages.points} de temps en temps.")
            )
        }
        extensionProvider.forEach(HelperExtension::class) { help ->
            help.registerCommand("!claim")
        }
    }

    private fun interceptCommandEvent(commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            "!claim" -> claim(commandEvent.login)
        }

        return commandEvent
    }

    private fun claim(login: String) {
        if (alreadyClaimed(login)) {
            return
        }

        rememberLastClaimIsNow(login)

        extensionProvider.provide(PointsManager::class)
            .filter { it.channel == channel }
            .forEach { pointsManager ->
                pointsManager.addPoints(login, claimedPoints)
            }
    }

    private fun alreadyClaimed(login: String): Boolean {
        val lastClaimedAt = storage.getUserInfo(login, namespace, "lastClaimedAt").tryToLong()
        return lastClaimedAt != null && lastClaimedAt + intervalBetweenTwoClaims > System.currentTimeMillis()
    }

    private fun rememberLastClaimIsNow(login: String) {
        storage.putUserInfo(login, namespace, "lastClaimedAt", System.currentTimeMillis().toString())
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private var intervalBetweenTwoClaims: Long = TimeUnit.HOURS.toMillis(12)
        private var claimedPoints: Int = 10

        private var messages: Messages? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun claim(points: Int, time: Long, unit: TimeUnit) {
            claimedPoints = points
            intervalBetweenTwoClaims = unit.toMillis(time)
        }

        @Dsl
        fun messages(
            points: String,
        ) {
            messages = Messages(
                points
            )
        }

        fun build(serviceLocator: ServiceLocator): Rewards {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Rewards::class.simpleName}")
            val theMessages = messages
                ?: throw IllegalStateException("Messages must be set for the extension ${Rewards::class.simpleName}")

            return Rewards(
                channel = channelName,
                extensionProvider = serviceLocator.extensionProvider,
                intervalBetweenTwoClaims = intervalBetweenTwoClaims,
                claimedPoints = claimedPoints,
                messages = theMessages
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Rewards> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Rewards {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { rewards ->
                    pipeline.interceptCommandEvent { _, commandEvent -> rewards.interceptCommandEvent(commandEvent) }
                    pipeline.requestChannel(rewards.channel)
                    rewards.onInstallationFinished()
                }
        }

    }
}