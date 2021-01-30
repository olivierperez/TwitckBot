package fr.o80.twitck.extension.points

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PointsConfiguration(
    val channel: String,
    val badges: Array<String>,
    val messages: PointsMessages
)

@JsonClass(generateAdapter = true)
class PointsMessages(
    val destinationViewerDoesNotExist: String,
    val pointsTransferred: String,
    val notEnoughPoints: String,
    val viewerHasNoPoints: String,
    val viewerHasPoints: String,
)
