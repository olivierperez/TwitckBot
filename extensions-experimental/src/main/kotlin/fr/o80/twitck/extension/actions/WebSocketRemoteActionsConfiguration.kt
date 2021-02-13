package fr.o80.twitck.extension.actions

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class WebSocketRemoteActionsConfiguration(
    val channel: ChannelName,
    val slobsHost: String,
    val slobsPort: Int,
    val slobsToken: String,
    val actionsPort: Int
)
