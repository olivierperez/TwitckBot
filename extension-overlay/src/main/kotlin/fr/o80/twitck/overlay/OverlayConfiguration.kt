package fr.o80.twitck.overlay

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class OverlayConfiguration(
    val informationText: String
)
