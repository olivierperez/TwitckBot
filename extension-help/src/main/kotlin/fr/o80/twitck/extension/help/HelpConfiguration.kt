package fr.o80.twitck.extension.help

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class HelpConfiguration(
    val channel: ChannelName,
    val commands: Map<String, String>
)
