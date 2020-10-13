package fr.o80.twitck.lib.api.bean.twitch

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwitchSubscriptionEvent(
    @Json(name = "id")
    val id: String,
    // subscriptions.subscribe -> someone has subscribed
    // subscriptions.notification -> notified broadcaster of their subscription in chat
    @Json(name = "event_type")
    val type: String,
    @Json(name = "event_data")
    val data: TwitchSubscriptionData
)
