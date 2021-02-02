package fr.o80.twitck.extension.channel.config

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.service.step.CommandStep

@JsonClass(generateAdapter = true)
class ChannelConfiguration(
    val channel: String,
    val commands: Map<String, List<CommandStep>>,
    val follows: List<CommandStep>
)
