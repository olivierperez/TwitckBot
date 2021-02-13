package fr.o80.twitck.extension.promotion

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class ViewerPromotionConfiguration(
    val channel: ChannelName,
    val secondsBetweenTwoPromotions: Long,
    val daysSinceLastVideoToPromote: Long,
    val ignoreViewers: List<String>,
    val promotionMessages: List<String>,
    val i18n: ViewerPromotionI18n
)

@JsonClass(generateAdapter = true)
class ViewerPromotionI18n(
    val usage: String,
    val noPointsEnough: String,
    val noAutoShoutOut: String,
    val shoutOutRecorded: String,
)
