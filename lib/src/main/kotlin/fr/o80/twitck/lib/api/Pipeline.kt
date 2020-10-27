package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.handler.CommandHandler
import fr.o80.twitck.lib.api.handler.FollowsHandler
import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.SubscriptionsHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler

interface Pipeline {
    fun interceptJoinEvent(joinHandler: JoinHandler)
    fun interceptMessageEvent(messageHandler: MessageHandler)
    fun interceptCommandEvent(commandHandler: CommandHandler)
    fun interceptWhisperEvent(whisperHandler: WhisperHandler)
    fun interceptFollowEvent(followsHandler: FollowsHandler)
    fun interceptSubscriptionEvent(subscriptionsHandler: SubscriptionsHandler)
    fun requestChannel(channel: String)
}

