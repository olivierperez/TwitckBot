package fr.o80.twitck.lib.api.bean.twitch

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwitchSubscriptionData(
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String,
    @Json(name = "broadcaster_id") val broadcasterId: String,
    @Json(name = "broadcaster_name") val broadcasterName: String,
    @Json(name = "gifter_id") val gifterId: String?,
    @Json(name = "gifter_name") val gifterName: String?,
    @Json(name = "plan_name") val planName: String?,
    @Json(name = "tier") val tier: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "is_gift") val isGift: Boolean
)
