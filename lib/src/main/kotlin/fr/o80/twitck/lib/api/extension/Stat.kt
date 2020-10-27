package fr.o80.twitck.lib.api.extension

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.FollowsEvent
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.bean.WhisperEvent
import fr.o80.twitck.lib.api.bean.subscription.SubscriptionEvent

interface Stat {
//    fun onBits(messageEvent: MessageEvent)
//    fun onCommand(commandEvent: CommandEvent)
//    fun onFollow(followsEvent: FollowsEvent)
//    fun onJoin(joinEvent: JoinEvent)
//    fun onMessage(messageEvent: MessageEvent)
//    fun onSubscription(subscriptionEvent: SubscriptionEvent)
//    fun onWhisper(whisperEvent: WhisperEvent)
    //fun onCustomEvent()

    fun increment(namespace: String, key: String, count: Long = 0)
    fun maximum(namespace: String, key: String, value: Long)
    fun minimum(namespace: String, key: String, value: Long)
}