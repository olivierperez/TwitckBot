package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.service.Messenger

data class MessageEvent(
    val messenger: Messenger,
    val channel: String,
    val message: String,
    val bits: Int,
    val viewer: Viewer
)
