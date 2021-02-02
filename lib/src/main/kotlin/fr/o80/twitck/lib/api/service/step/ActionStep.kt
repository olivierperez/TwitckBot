package fr.o80.twitck.lib.api.service.step

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class ActionStep(
    @Json(name = "type")
    @Suppress("unused")
    val type: Type
) {
    enum class Type(val value: String) {
        COMMAND("command"),
        SOUND("sound"),
        OVERLAY("overlay"),
        MESSAGE("message")
    }
}

@JsonClass(generateAdapter = true)
internal class MessageStep(
    val message: String
) : ActionStep(Type.MESSAGE)

@JsonClass(generateAdapter = true)
internal class SoundStep(
    @Json(name = "sound")
    val soundId: String
) : ActionStep(Type.SOUND)

@JsonClass(generateAdapter = true)
internal class OverlayStep(
    val image: String,
    val text: String,
    val seconds: Long = 5
) : ActionStep(Type.OVERLAY)

@JsonClass(generateAdapter = true)
internal class CommandStep(
    val command: String
) : ActionStep(Type.COMMAND)
