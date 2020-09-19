package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.service.Messenger

data class CommandEvent(
    val messenger: Messenger,
    val channel: String,
    val login: String,
    val userId: String,
    val badges: List<Badge>,
    val command: Command
)
