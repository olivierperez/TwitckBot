package fr.o80.twitck.overlay

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.Color

@JsonClass(generateAdapter = true)
class OverlayConfiguration(
    val informationText: String,
    val style: OverlayStyle
)

@JsonClass(generateAdapter = true)
class OverlayStyle(
    val borderColor: Color,
    val backgroundColor: Color,
    val textColor: Color
)
