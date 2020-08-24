package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler
import fr.o80.twitck.lib.api.service.log.LoggerFactory

class TwitckConfiguration(
    val oauthToken: String,
    val requestedChannels: Iterable<String>,
    val joinHandlers: List<JoinHandler>,
    val messageHandlers: List<MessageHandler>,
    val whisperHandlers: List<WhisperHandler>,
    val loggerFactory: LoggerFactory
)
