package fr.o80.twitck.lib.api.bean

import fr.o80.twitck.lib.api.handler.CommandHandler
import fr.o80.twitck.lib.api.handler.FollowHandler
import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.LoggerFactory

class TwitckConfiguration(
    val oauthToken: String,
    val hostName: String,
    val requestedChannels: Iterable<String>,
    val joinHandlers: List<JoinHandler>,
    val messageHandlers: List<MessageHandler>,
    val commandHandlers: List<CommandHandler>,
    val whisperHandlers: List<WhisperHandler>,
    val followHandlers: List<FollowHandler>,
    val loggerFactory: LoggerFactory,
    val commandParser: CommandParser,
    val twitchApi: TwitchApi
)
