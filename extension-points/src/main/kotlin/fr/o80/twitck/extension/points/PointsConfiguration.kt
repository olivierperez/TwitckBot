package fr.o80.twitck.extension.points

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class PointsConfiguration(
    val channel: ChannelName,
    val privilegedBadges: Collection<Badge>,
    val i18n: PointsI18n
)

@JsonClass(generateAdapter = true)
class PointsI18n(
    val destinationViewerDoesNotExist: String,
    val pointsTransferred: String,
    val notEnoughPoints: String,
    val viewerHasNoPoints: String,
    val viewerHasPoints: String,
)
