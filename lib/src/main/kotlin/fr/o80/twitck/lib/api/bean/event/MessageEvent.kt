package fr.o80.twitck.lib.api.bean.event

import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.service.Messenger

data class MessageEvent(
    val messenger: Messenger,
    val channel: String,
    val message: String,
    val bits: Int,
    val viewer: Viewer
)
