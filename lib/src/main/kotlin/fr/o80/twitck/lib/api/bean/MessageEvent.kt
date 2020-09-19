package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.Messenger

data class MessageEvent(
    val messenger: Messenger,
    val channel: String,
    val login: String,
    val userId: String,
    val badges: List<Badge>,
    val message: String
)
