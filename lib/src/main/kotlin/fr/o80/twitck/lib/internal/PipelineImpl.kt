package fr.o80.twitck.lib.internal

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.handler.CommandHandler
import fr.o80.twitck.lib.api.handler.FollowsHandler
import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.SubscriptionsHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler

internal class PipelineImpl : Pipeline {
    val joinHandlers: MutableList<JoinHandler> = mutableListOf()
    val messageHandlers: MutableList<MessageHandler> = mutableListOf()
    val commandHandlers: MutableList<CommandHandler> = mutableListOf()
    val whisperHandlers: MutableList<WhisperHandler> = mutableListOf()
    val followsHandlers: MutableList<FollowsHandler> = mutableListOf()
    val subscriptionsHandlers: MutableList<SubscriptionsHandler> = mutableListOf()
    val requestedChannels: MutableSet<String> = mutableSetOf()

    override fun interceptJoinEvent(joinHandler: JoinHandler) {
        joinHandlers += joinHandler
    }

    override fun interceptMessageEvent(messageHandler: MessageHandler) {
        messageHandlers += messageHandler
    }

    override fun interceptCommandEvent(commandHandler: CommandHandler) {
        commandHandlers += commandHandler
    }

    override fun interceptWhisperEvent(whisperHandler: WhisperHandler) {
        whisperHandlers += whisperHandler
    }

    override fun interceptFollowEvent(followsHandler: FollowsHandler) {
        followsHandlers += followsHandler
    }

    override fun interceptSubscriptionEvent(subscriptionsHandler: SubscriptionsHandler) {
        subscriptionsHandlers += subscriptionsHandler
    }

    override fun requestChannel(channel: String) {
        requestedChannels += channel
    }
}