package fr.o80.twitck.extension.channel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ChannelConfiguration(
    val channel: String,
    val commands: Map<String, List<CommandStep>>
)

sealed class CommandStep(
    @Json(name = "type")
    @Suppress("unused")
    val type: CommandStepType
)

@JsonClass(generateAdapter = true)
class SoundStep(
    @Json(name = "sound")
    val soundId: String
) : CommandStep(CommandStepType.SOUND)

@JsonClass(generateAdapter = true)
class OverlayStep(
    val image: String,
    val text: String,
    val seconds: Long = 5
) : CommandStep(CommandStepType.OVERLAY)

enum class CommandStepType(val value: String) {
    SOUND("sound"), OVERLAY("overlay")
}
