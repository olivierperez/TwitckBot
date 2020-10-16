package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.bean.twitch.TwitchSubscriptionData

sealed class SubscriptionEvent(
    val events: List<TwitchSubscriptionData>
)

class NewSubsEvent(
    events: List<TwitchSubscriptionData>
) : SubscriptionEvent(events)

class SubNotificationsEvent(
    events: List<TwitchSubscriptionData>
) : SubscriptionEvent(events)

class UnknownSubEvent(
    val eventType: String,
    events: List<TwitchSubscriptionData>
) : SubscriptionEvent(events)
