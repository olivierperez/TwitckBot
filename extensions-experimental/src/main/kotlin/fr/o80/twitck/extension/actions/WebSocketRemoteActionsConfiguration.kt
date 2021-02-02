package fr.o80.twitck.extension.actions

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WebSocketRemoteActionsConfiguration(
    val channel: String,
    val slobsHost: String,
    val slobsPort: Int,
    val slobsToken: String
)
