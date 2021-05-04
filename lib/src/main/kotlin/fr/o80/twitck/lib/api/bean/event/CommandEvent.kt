package fr.o80.twitck.lib.api.bean.event

import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.service.Messenger

data class CommandEvent(
    val messenger: Messenger,
    val channel: String,
    val command: Command,
    val bits: Int?,
    val viewer: Viewer
)
