package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.ServiceLocator

class Points(
    override val channel: String,
    private val commandParser: CommandParser,
    private val extensionProvider: ExtensionProvider,
    private val commands: PointsCommands,
    private val bank: PointsBank,
    private val messages: Messages
) : PointsManager {

    private val namespace: String = Points::class.java.name

    override fun getPoints(login: String): Int {
        return bank.getPoints(login)
    }

    override fun addPoints(login: String, points: Int) {
        bank.addPoints(login, points)
    }

    override fun consumePoints(login: String, points: Int): Boolean {
        return bank.removePoints(login, points)
    }

    private fun interceptMessage(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        commandParser.parse(messageEvent)?.let { command ->
            commands.reactTo(command)
        }

        return messageEvent
    }

    private fun afterInstallation() {
        extensionProvider.provide(Overlay::class.java).forEach { overlay ->
            overlay.provideInformation(namespace, listOf("Vous avez combien de ${messages.points} ? !points_info"))
        }
        extensionProvider.provide(HelperExtension::class.java).forEach { help ->
            help.registerCommand("!points_info")
        }
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null
        private var badges: Array<out Badge>? = null

        private var messages : Messages? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun privilegedBadges(vararg badges: Badge) {
            if (badges.isEmpty()) {
                throw IllegalArgumentException("Impossible to set an empty list of privileged badges.")
            }
            this.badges = badges
        }

        @Dsl
        fun messages(
            pointsTransferred: String,
            noPointsEnough: String,
            viewHasNoPoints: String,
            viewHasPoints: String,
            points: String,
        ) {
            messages = Messages(pointsTransferred, noPointsEnough, viewHasNoPoints, viewHasPoints, points)
        }

        fun build(serviceLocator: ServiceLocator): Points {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Points::class.simpleName}")
            val privilegedBadges = badges
                ?: arrayOf(Badge.BROADCASTER)
            val bank = PointsBank(serviceLocator.extensionProvider)
            val theMessages = messages
                ?: throw IllegalStateException("Messages must be set for the extension ${Points::class.simpleName}")

            val logger = serviceLocator.loggerFactory.getLogger(Points::class)

            val pointsCommands = PointsCommands(
                privilegedBadges,
                bank,
                theMessages,
                serviceLocator.extensionProvider,
                logger
            )
            return Points(
                channelName,
                serviceLocator.commandParser,
                serviceLocator.extensionProvider,
                pointsCommands,
                bank,
                theMessages
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Points> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Points {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { points ->
                    pipeline.interceptMessageEvent { bot, messageEvent -> points.interceptMessage(bot, messageEvent) }
                    pipeline.requestChannel(points.channel)
                    points.afterInstallation()
                }
        }

    }
}