package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.service.log.LoggerFactory
import fr.o80.twitck.lib.internal.service.CoolDownManager

interface ServiceLocator {
    val extensionProvider: ExtensionProvider
    val commandParser: CommandParser
    val loggerFactory: LoggerFactory
    val twitchApi: TwitchApi
    val coolDownManager: CoolDownManager
}

class ServiceLocatorImpl(
    override val extensionProvider: ExtensionProvider,
    override val loggerFactory: LoggerFactory,
    override val twitchApi: TwitchApi,
    override val commandParser: CommandParser = CommandParser(),
    override val coolDownManager: CoolDownManager = CoolDownManager()
) : ServiceLocator
