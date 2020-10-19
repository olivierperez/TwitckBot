package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.service.Messenger

data class CommandEvent(
    val messenger: Messenger,
    val channel: String,
    val command: Command,
    val bits: Int,
    val viewer: Viewer
)
