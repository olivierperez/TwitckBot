package fr.o80.twitck.extension.runtimecommand

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.Badge

@JsonClass(generateAdapter = true)
class RuntimeCommandConfiguration(
    val channel: String,
    val privilegedBadges: List<Badge>
)
