package fr.o80.twitck.lib.api.bean.subscription

import fr.o80.twitck.lib.api.bean.twitch.TwitchSubscriptionData

class SubscriptionEvent(
    val eventType: SubscriptionEventType,
    val events: List<TwitchSubscriptionData>
)
