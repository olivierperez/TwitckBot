package fr.o80.twitck.poll

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.ServiceLocator

class Poll(
    private val channel: String,
    private val commands: PollCommands,
    private val commandParser: CommandParser
) {

    private fun interceptMessage(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        // TODO Ajouter au Pipeline, une méthode interceptCommand(Bot, Command)
        commandParser.parse(messageEvent)?.let { command ->
            commands.reactTo(bot, command)
        }

        return messageEvent
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null
        private var badges: Array<out Badge>? = null

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

        fun build(serviceLocator: ServiceLocator): Poll {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Poll::class.simpleName}")
            val privilegedBadges = badges
                ?: arrayOf(Badge.BROADCASTER)

            return Poll(
                channelName,
                PollCommands(
                    channel = channelName,
                    privilegedBadges = privilegedBadges
                ),
                commandParser = serviceLocator.commandParser
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Poll> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Poll {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { poll ->
                    pipeline.interceptMessageEvent(poll::interceptMessage)
                }
        }

    }
}