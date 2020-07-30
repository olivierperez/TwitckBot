package fr.o80.twitck.lib.bean

import fr.o80.twitck.lib.bot.TwitckBot

class JoinEvent(
    val bot: TwitckBot,
    val channel: String,
    val login: String
)
