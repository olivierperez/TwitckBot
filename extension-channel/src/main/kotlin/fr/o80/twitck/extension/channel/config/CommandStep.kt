package fr.o80.twitck.extension.channel.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class CommandStep(
    @Json(name = "type")
    @Suppress("unused")
    val type: Type
) {
    enum class Type(val value: String) {
        SOUND("sound"),
        OVERLAY("overlay"),
        MESSAGE("message")
    }
}

@JsonClass(generateAdapter = true)
class MessageStep(
    val message: String
) : CommandStep(Type.MESSAGE)

@JsonClass(generateAdapter = true)
class SoundStep(
    @Json(name = "sound")
    val soundId: String
) : CommandStep(Type.SOUND)

@JsonClass(generateAdapter = true)
class OverlayStep(
    val image: String,
    val text: String,
    val seconds: Long = 5
) : CommandStep(Type.OVERLAY)
