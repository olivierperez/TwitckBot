package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.log.LoggerFactory
import fr.o80.twitck.lib.api.service.step.StepsExecutor
import fr.o80.twitck.lib.internal.service.CoolDownManager
import fr.o80.twitck.lib.internal.service.step.StepFormatter
import fr.o80.twitck.lib.internal.service.step.StepsExecutorImpl

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
    override val commandParser: CommandParser,
    override val coolDownManager: CoolDownManager = CoolDownManager()
) : ServiceLocator {
    override val stepsExecutor: StepsExecutor
        get() = StepsExecutorImpl(
            commandTriggering,
            commandParser,
            StepFormatter(),
            loggerFactory.getLogger(StepsExecutorImpl::class),
            extensionProvider.firstOrNull(OverlayExtension::class),
            extensionProvider.firstOrNull(SoundExtension::class)
        )
}
