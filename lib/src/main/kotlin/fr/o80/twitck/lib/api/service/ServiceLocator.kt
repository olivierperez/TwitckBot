package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.service.log.LoggerFactory

class ServiceLocator(
    val extensionProvider: ExtensionProvider,
    val commandParser: CommandParser = CommandParser(),
    val loggerFactory: LoggerFactory,
    val twitchApi: TwitchApi
)
