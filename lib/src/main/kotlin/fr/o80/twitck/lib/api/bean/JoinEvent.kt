package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.TwitckBot

class JoinEvent(
    val bot: TwitckBot,
    val channel: String,
    val viewer: Viewer
)
