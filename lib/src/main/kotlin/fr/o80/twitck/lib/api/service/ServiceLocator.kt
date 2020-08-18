package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.extension.ExtensionProvider

class ServiceLocator(
    val extensionProvider: ExtensionProvider,
    val commandParser: CommandParser = CommandParser(),
    val twitchApi: TwitchApi
)
