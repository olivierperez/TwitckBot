package fr.o80.twitck.extension.channel.config

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ChannelConfiguration(
    val channel: String,
    val commands: Map<String, List<CommandStep>>,
    val follows: List<CommandStep>
)
