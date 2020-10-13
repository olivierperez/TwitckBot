package fr.o80.twitck.extension.channel

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.FollowsEvent
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger

/**
 * This extension provides basic configuration for a given channel.
 * It is able to react to some events :
 * - Messages
 * - Someone joined
 */
class Channel(
    private val channel: String,
    private val joinCallbacks: Iterable<JoinCallback>,
    private val followCallbacks: Iterable<FollowCallback>,
    private val commandCallbacks: Iterable<Pair<String, CommandCallback>>,
    private val logger: Logger
) {

    fun interceptJoinEvent(messenger: Messenger, joinEvent: JoinEvent): JoinEvent {
        if (channel != joinEvent.channel)
            return joinEvent

        logger.trace("I've just seen a join event: ${joinEvent.channel} > ${joinEvent.login}")

        joinCallbacks.forEach { callback ->
            callback(messenger, joinEvent)

        }

        return joinEvent
    }

    fun interceptFollowEvent(messenger: Messenger, followsEvent: FollowsEvent): FollowsEvent {
        logger.debug("New followers intercepted: ${followsEvent.followers}")
        followCallbacks.forEach { callback ->
            logger.debug("New followers callback called: $callback")
            callback(messenger, followsEvent)
        }

        return followsEvent
    }

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        commandCallbacks.forEach { (commandTag, callback) ->
            if (commandTag == commandEvent.command.tag) {
                callback(messenger, commandEvent)
            }
        }

        return commandEvent
    }

    class Configuration {

        @DslMarker
        private annotation class ChannelDsl

        private var channel: String? = null

        private val joinCallbacks: MutableList<JoinCallback> = mutableListOf()
        private val followCallbacks: MutableList<FollowCallback> = mutableListOf()
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

        @ChannelDsl
        fun follow(callback: FollowCallback) {
            followCallbacks += callback
        }

        fun build(serviceLocator: ServiceLocator): Channel {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Channel::class.simpleName}")
            return Channel(
                channel = channelName,
                joinCallbacks = joinCallbacks,
                followCallbacks = followCallbacks,
                commandCallbacks = commandCallbacks,
                logger = serviceLocator.loggerFactory.getLogger(Channel::class)
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
                    pipeline.interceptCommandEvent(channel::interceptCommandEvent)
                    pipeline.interceptFollowHandler(channel::interceptFollowEvent)
                    pipeline.requestChannel(channel.channel)
                }
        }
    }
}

typealias CommandCallback = (
    messenger: Messenger,
    commandEvent: CommandEvent
) -> Unit

typealias JoinCallback = (
    messenger: Messenger,
    joinEvent: JoinEvent
) -> Unit

typealias FollowCallback = (
    messenger: Messenger,
    followsEvent: FollowsEvent
) -> Unit
