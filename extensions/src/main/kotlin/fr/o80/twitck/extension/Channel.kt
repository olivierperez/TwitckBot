package fr.o80.twitck.extension

import fr.o80.twitck.lib.ExtensionProvider
import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.bean.JoinEvent
import fr.o80.twitck.lib.bean.MessageEvent
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.extension.TwitckExtension

/**
 * This extension provides basic configuration for a given channel.
 * It is able to react to some events :
 * - Messages
 * - Someone joined
 */
class Channel(
    private val channel: String,
    private val joinCallbacks: Iterable<JoinCallback>,
    private val commandCallbacks: Iterable<Pair<String, CommandCallback>>
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

        println("> I've just seen a message event: ${messageEvent.channel} > ${messageEvent.message}")

        val command = parseCommand(messageEvent)

        commandCallbacks.forEach { (commandTag, callback) ->
            if (commandTag == command.tag) {
                callback(bot, command)
            }
        }

        return messageEvent
    }

    private fun parseCommand(messageEvent: MessageEvent): Command {
        val split = messageEvent.message.split(" ")
        return if (split.size == 1) {
            Command(split[0])
        } else {
            Command(split[0], split.subList(1, split.size))
        }
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

        fun build(): Channel {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Channel::class.simpleName}")
            return Channel(
                channel = channelName,
                joinCallbacks = joinCallbacks,
                commandCallbacks = commandCallbacks
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Channel> {
        override fun install(pipeline: Pipeline, extensionProvider: ExtensionProvider, configure: Configuration.() -> Unit): Channel {
            return Configuration()
                .apply(configure)
                .build()
                .also { channel ->
                    pipeline.interceptJoinEvent(channel::interceptJoinEvent)
                    pipeline.interceptMessageEvent(channel::interceptMessageEvent)
                    pipeline.requestChannel(channel.channel)
                }
        }
    }
}

class Command(
    val tag: String,
    val options: List<String> = emptyList()
)

typealias CommandCallback = (
    bot: TwitckBot,
    command: Command
) -> Unit

typealias JoinCallback = (
    bot: TwitckBot,
    joinEvent: JoinEvent
) -> Unit
