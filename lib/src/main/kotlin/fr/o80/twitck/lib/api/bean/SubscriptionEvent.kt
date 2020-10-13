package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.bean.twitch.TwitchSubscriptionEvent

sealed class SubscriptionEvent

class NewSubscriptionsEvent(
    val events: List<TwitchSubscriptionEvent>
) : SubscriptionEvent()

class NotificationSubscriptionsEvent(
    val events: List<TwitchSubscriptionEvent>
) : SubscriptionEvent()

class UnknownSubscriptionsEvent(
    val eventType: String,
    val events: List<TwitchSubscriptionEvent>
) : SubscriptionEvent()
