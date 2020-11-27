package fr.o80.twitck.lib.api.bean

class WhisperEvent(
    val destination: String,
    val viewer: Viewer,
    val message: String
)
