package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.service.log.LoggerFactory
import fr.o80.twitck.lib.api.service.step.StepsExecutor
import fr.o80.twitck.lib.internal.service.CoolDownManager

interface ServiceLocator {
    val commandParser: CommandParser
    val commandTriggering: CommandTriggering
    val coolDownManager: CoolDownManager
    val extensionProvider: ExtensionProvider
    val loggerFactory: LoggerFactory
    val stepsExecutor: StepsExecutor
    val twitchApi: TwitchApi
}

class ServiceLocatorImpl(
    override val extensionProvider: ExtensionProvider,
    override val loggerFactory: LoggerFactory,
    override val twitchApi: TwitchApi,
    override val commandTriggering: CommandTriggering,
    override val stepsExecutor: StepsExecutor,
    override val commandParser: CommandParser,
    override val coolDownManager: CoolDownManager = CoolDownManager()
) : ServiceLocator
