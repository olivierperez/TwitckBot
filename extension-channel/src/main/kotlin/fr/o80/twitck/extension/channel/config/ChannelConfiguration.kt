package fr.o80.twitck.extension.channel.config

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.ChannelName
import fr.o80.twitck.lib.api.service.step.ActionStep

@JsonClass(generateAdapter = true)
class ChannelConfiguration(
    val channel: ChannelName,
    val commands: Map<String, List<ActionStep>>,
    val follows: List<ActionStep>
)
