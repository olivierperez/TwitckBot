package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.bean.twitch.TwitchSubscriptionData

sealed class SubscriptionEvent

class NewSubsEvent(
    val events: List<TwitchSubscriptionData>
) : SubscriptionEvent()

class SubNotificationsEvent(
    val events: List<TwitchSubscriptionData>
) : SubscriptionEvent()

class UnknownSubEvent(
    val eventType: String,
    val events: List<TwitchSubscriptionData>
) : SubscriptionEvent()
