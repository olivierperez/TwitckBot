package fr.o80.twitck.lib.service

import fr.o80.twitck.lib.extension.ExtensionProvider

class ServiceLocator(
    val extensionProvider: ExtensionProvider,
    val commandParser: CommandParser = CommandParser()
)
