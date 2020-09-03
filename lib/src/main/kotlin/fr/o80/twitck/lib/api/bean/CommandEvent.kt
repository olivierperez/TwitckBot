package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.TwitckBot

data class CommandEvent(
    val bot: TwitckBot,
    val channel: String,
    val login: String,
    val userId: String,
    val badges: List<Badge>,
    val command: Command
)
