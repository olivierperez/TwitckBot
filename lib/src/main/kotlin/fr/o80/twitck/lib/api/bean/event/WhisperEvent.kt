package fr.o80.twitck.lib.api.bean.event

import fr.o80.twitck.lib.api.bean.Viewer

class WhisperEvent(
    val destination: String,
    val viewer: Viewer,
    val message: String
)
