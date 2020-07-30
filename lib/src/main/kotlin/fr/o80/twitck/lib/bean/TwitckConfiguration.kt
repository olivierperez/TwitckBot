package fr.o80.twitck.lib.bean

import fr.o80.twitck.lib.handler.JoinHandler
import fr.o80.twitck.lib.handler.MessageHandler
import fr.o80.twitck.lib.handler.WhisperHandler

class TwitckConfiguration(
    val requestedChannels: Iterable<String>,
    val joinHandlers: List<JoinHandler>,
    val messageHandlers: List<MessageHandler>,
    val whisperHandlers: List<WhisperHandler>
)
