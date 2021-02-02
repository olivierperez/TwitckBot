package fr.o80.twitck.extension.sound

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SoundConfiguration(
    val celebration: OneSound,
    val negative: OneSound,
    val positive: OneSound,
    val raid: OneSound,
    val custom: Map<String, OneSound>
)

@JsonClass(generateAdapter = true)
class OneSound(
    val path: String,
    val gain: Float
)
