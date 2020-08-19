package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.ServiceLocator

/**
 * This extension provides basic configuration for a given channel.
 * It is able to react to some events :
 * - Messages
 * - Someone joined
 */
class Channel(
    private val channel: String,
    private val joinCallbacks: Iterable<JoinCallback>,
    private val commandCallbacks: Iterable<Pair<String, CommandCallback>>,
    private val commandParser: CommandParser
) {

    fun interceptJoinEvent(bot: TwitckBot, joinEvent: JoinEvent): JoinEvent {
        if (channel != joinEvent.channel)
            return joinEvent

        println("> I've just seen a join event: ${joinEvent.channel} > ${joinEvent.login}")

        joinCallbacks.forEach { callback ->
            callback(bot, joinEvent)

        }

        return joinEvent
    }

    fun interceptMessageEvent(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        val command = commandParser.parse(messageEvent)

        commandCallbacks.forEach { (commandTag, callback) ->
            if (commandTag == command.tag) {
                callback(bot, command)
            }
        }

        return messageEvent
    }

    class Configuration {

        @DslMarker
        annotation class ChannelDsl

        private var channel: String? = null

        private val joinCallbacks: MutableList<JoinCallback> = mutableListOf()
        private val commandCallbacks: MutableList<Pair<String, CommandCallback>> = mutableListOf()

        @ChannelDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @ChannelDsl
        fun command(command: String, callback: CommandCallback) {
            commandCallbacks += command to callback
        }

        @ChannelDsl
        fun join(callback: JoinCallback) {
            joinCallbacks += callback
        }

        fun build(serviceLocator: ServiceLocator): Channel {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Channel::class.simpleName}")
            return Channel(
                channel = channelName,
                joinCallbacks = joinCallbacks,
                commandCallbacks = commandCallbacks,
                commandParser = serviceLocator.commandParser
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Channel> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Channel {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { channel ->
                    pipeline.interceptJoinEvent(channel::interceptJoinEvent)
                    pipeline.interceptMessageEvent(channel::interceptMessageEvent)
                    pipeline.requestChannel(channel.channel)
                }
        }
    }
}

typealias CommandCallback = (
    bot: TwitckBot,
    command: Command
) -> Unit

typealias JoinCallback = (
    bot: TwitckBot,
    joinEvent: JoinEvent
) -> Unit
