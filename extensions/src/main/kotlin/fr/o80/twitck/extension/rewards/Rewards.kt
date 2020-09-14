package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import java.util.concurrent.TimeUnit

class Rewards(
    private val channel: String,
    private val commands: RewardsCommands,
    private val extensionProvider: ExtensionProvider,
    private val messages: Messages
) {

    // TODO OPZ !! Récompenser les gens qui parlent

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
            viewerJustClaimed: String,
        ) {
            messages = Messages(
                points,
                viewerJustClaimed
            )
        }

        fun build(serviceLocator: ServiceLocator): Rewards {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Rewards::class.simpleName}")
            val theMessages = messages
                ?: throw IllegalStateException("Messages must be set for the extension ${Rewards::class.simpleName}")

            val commands = RewardsCommands(
                channel = channelName,
                extensionProvider = serviceLocator.extensionProvider,
                intervalBetweenTwoClaims = intervalBetweenTwoClaims,
                claimedPoints = claimedPoints,
                messages = theMessages
            )
            return Rewards(
                channel = channelName,
                commands = commands,
                extensionProvider = serviceLocator.extensionProvider,
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
                    pipeline.interceptCommandEvent(rewards.commands::interceptCommandEvent)
                    pipeline.requestChannel(rewards.channel)
                    rewards.onInstallationFinished()
                }
        }

    }
}