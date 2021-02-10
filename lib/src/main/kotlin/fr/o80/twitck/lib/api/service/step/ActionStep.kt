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
        MESSAGE("message"),
        OVERLAY_EVENT("overlay_event"),
        OVERLAY_POPUP("overlay_popup"),
        SOUND("sound")
    }
}

@JsonClass(generateAdapter = true)
internal class CommandStep(
    val command: String
) : ActionStep(Type.COMMAND)

@JsonClass(generateAdapter = true)
internal class MessageStep(
    val message: String
) : ActionStep(Type.MESSAGE)

@JsonClass(generateAdapter = true)
internal class OverlayEventStep(
    val text: String
) : ActionStep(Type.OVERLAY_EVENT)

@JsonClass(generateAdapter = true)
internal class OverlayPopupStep(
    val image: String,
    val text: String,
    val seconds: Long = 5
) : ActionStep(Type.OVERLAY_POPUP)

@JsonClass(generateAdapter = true)
internal class SoundStep(
    @Json(name = "sound")
    val soundId: String
) : ActionStep(Type.SOUND)
