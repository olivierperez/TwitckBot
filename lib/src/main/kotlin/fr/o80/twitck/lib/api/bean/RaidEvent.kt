package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.service.Messenger

data class RaidEvent(
    val messenger: Messenger,
    val channel: String,
    val viewer: Viewer,
    val msgLogin: String,
    val msgDisplayName: String,
    val msgViewerCount: String
)
