package fr.o80.twitck.lib.internal.service

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import fr.o80.twitck.lib.api.bean.Color

class ColorAdapter {
    @ToJson
    fun toJson(color: Color): String {
        val redStr = color.red.toString(16)
        val greenStr = color.green.toString(16)
        val blueStr = color.blue.toString(16)
        return "#$redStr$greenStr$blueStr"
    }

    @FromJson
    fun fromJson(rgb: String): Color {
        val colors = rgb.substring(1)
            .chunked(2)
            .map { Integer.parseInt(it, 16) }

        return Color(
            colors[0],
            colors[1],
            colors[2]
        )
    }

}
