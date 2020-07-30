package fr.o80.twitck.lib.bean

import fr.o80.twitck.lib.bot.TwitckBot

class MessageEvent(
    val bot: TwitckBot,
    val channel: String,
    val login: String,
    val message: String,
    val badges: List<Badge>
)
