package fr.o80.twitck.lib.service

import fr.o80.twitck.lib.extension.ExtensionProvider

class ServiceLocator(
    private val extensionProvider: ExtensionProvider,
    private val commandParser: CommandParser = CommandParser()
) {
    // TODO OPZ Lazy ou bien ?

    fun provideExtensionProvider(): ExtensionProvider = extensionProvider
    fun provideCommandParser(): CommandParser = commandParser
}