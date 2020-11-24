package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.handler.*

interface Pipeline {
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
