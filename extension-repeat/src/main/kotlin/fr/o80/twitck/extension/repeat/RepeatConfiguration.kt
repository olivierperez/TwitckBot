package fr.o80.twitck.extension.repeat

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class RepeatConfiguration(
    val channel: ChannelName,
    val secondsBetweenRepeatedMessages: Long,
    val messages: List<String>,
)
