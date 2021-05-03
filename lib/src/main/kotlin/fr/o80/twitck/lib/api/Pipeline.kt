package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.handler.BitsHandler
import fr.o80.twitck.lib.api.handler.CommandHandler
import fr.o80.twitck.lib.api.handler.FollowsHandler
import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.RaidHandler
import fr.o80.twitck.lib.api.handler.SubscriptionsHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler

interface Pipeline {
    fun interceptBitsEvent(bitsHandler: BitsHandler)
    fun interceptCommandEvent(commandHandler: CommandHandler)
    fun interceptJoinEvent(joinHandler: JoinHandler)
    fun interceptFollowEvent(followsHandler: FollowsHandler)
    fun interceptMessageEvent(messageHandler: MessageHandler)
    fun interceptSubscriptionEvent(subscriptionsHandler: SubscriptionsHandler)
    fun interceptRaidEvent(raidHandler: RaidHandler)
    fun interceptWhisperCommandEvent(commandHandler: CommandHandler)
    fun interceptWhisperEvent(whisperHandler: WhisperHandler)
    fun requestChannel(channel: String)
}
