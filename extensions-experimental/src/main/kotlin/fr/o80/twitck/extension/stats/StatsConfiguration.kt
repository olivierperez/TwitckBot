package fr.o80.twitck.extension.stats

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class StatsConfiguration(
    val channel: ChannelName
)