package fr.o80.twitck.lib.api.bean.twitch

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwitchSubscriptionEvents(
    val data: List<TwitchSubscriptionEvent>
)
