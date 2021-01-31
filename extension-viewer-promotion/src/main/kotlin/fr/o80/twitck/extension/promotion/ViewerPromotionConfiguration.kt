package fr.o80.twitck.extension.promotion

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ViewerPromotionConfiguration(
    val channel: String,
    val secondsBetweenTwoPromotions: Long,
    val daysSinceLastVideoToPromote: Long,
    val ignoreViewers: List<String>,
    val promotionMessages: List<String>,
    val messages: ViewerPromotionMessages
)

@JsonClass(generateAdapter = true)
class ViewerPromotionMessages(
    val usage: String,
    val noPointsEnough: String,
    val noAutoShoutOut: String,
    val shoutOutRecorded: String,
)
