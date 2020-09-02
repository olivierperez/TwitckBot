package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.utils.tryToLong
import java.util.concurrent.TimeUnit

class Rewards(
    private val channel: String,
    private val commandParser: CommandParser,
    private val extensionProvider: ExtensionProvider,
    private val intervalBetweenTwoClaims: Long,
    private val claimedPoints: Int
) {

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = Rewards::class.java.name

    private fun interceptMessage(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        commandParser.parse(messageEvent)?.let { command ->
            handleCommand(command)
        }
        return messageEvent
    }

    private fun afterInstallation() {
        extensionProvider.provide(Overlay::class).forEach { overlay ->
            // TODO Remplacer POINTS par un message passé en config
            overlay.provideInformation(namespace, listOf("Vous pouvez !claim des POINTS de temps en temps."))
        }
        extensionProvider.provide(HelperExtension::class).forEach { help ->
            help.registerCommand("!claim")
        }
    }

    private fun handleCommand(command: Command) {
        when (command.tag) {
            "!claim" -> claim(command.login)
        }
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

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun claim(points: Int, time: Long, unit: TimeUnit) {
            claimedPoints = points
            intervalBetweenTwoClaims = unit.toMillis(time)
        }

        fun build(serviceLocator: ServiceLocator): Rewards {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Rewards::class.simpleName}")

            return Rewards(
                channel = channelName,
                commandParser = serviceLocator.commandParser,
                extensionProvider = serviceLocator.extensionProvider,
                claimedPoints = claimedPoints,
                intervalBetweenTwoClaims = intervalBetweenTwoClaims
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
                    pipeline.interceptMessageEvent(rewards::interceptMessage)
                    pipeline.requestChannel(rewards.channel)
                    rewards.afterInstallation()
                }
        }

    }
}