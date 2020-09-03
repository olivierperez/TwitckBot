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

        private var points: Int = 0
        private var messages: Messages? = null

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
        fun pointsToEarn(points: Int) {
            this.points = points
        }

        @Dsl
        fun messages(
            errorCreationPollUsage: String = "To create a poll: \"!poll <duration> <question>\"",
            errorDurationIsMissing: String = "You have to enter a duration for the poll!",
            newPoll: String = "New poll: #TITLE#",
            pollHasJustFinished: String = "Poll has just finished, to the question #TITLE# You've answered #BEST# #COUNT times",
            currentPollResult: String = "Poll is still running, the question is #TITLE# For now you've answered #BEST# #COUNT times",
            pollHasNoVotes: String = "No one has answered to the question #TITLE#"
        ) {
            messages = Messages(
                errorCreationPollUsage,
                errorDurationIsMissing,
                newPoll,
                pollHasJustFinished,
                currentPollResult,
                pollHasNoVotes
            )
        }

        fun build(serviceLocator: ServiceLocator): Poll {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Poll::class.simpleName}")
            val privilegedBadges = badges
                ?: arrayOf(Badge.BROADCASTER)
            val theMessages = messages
                ?: throw IllegalStateException("The messages must be set for the extension ${Poll::class.simpleName}")

            return Poll(
                channelName,
                PollCommands(
                    channel = channelName,
                    privilegedBadges = privilegedBadges,
                    messages = theMessages,
                    pointsForEachVote = points,
                    extensionProvider = serviceLocator.extensionProvider
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