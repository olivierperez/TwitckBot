package fr.o80.twitck.extension.runtimecommand

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class RuntimeCommandConfiguration(
    val channel: ChannelName,
    val privilegedBadges: List<Badge>
)
