package fr.o80.twitck.lib.api.bean.event

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Viewer

class JoinEvent(
    val bot: TwitckBot,
    val channel: String,
    val viewer: Viewer
)
