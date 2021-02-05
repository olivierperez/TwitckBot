package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.bean.TwitckConfiguration
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.CommandTriggeringImpl
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.ServiceLocatorImpl
import fr.o80.twitck.lib.api.service.log.Slf4jLoggerFactory
import fr.o80.twitck.lib.api.service.step.StepsExecutor
import fr.o80.twitck.lib.internal.PipelineImpl
import fr.o80.twitck.lib.internal.TwitckBotImpl
import fr.o80.twitck.lib.internal.service.ConfigService
import fr.o80.twitck.lib.internal.service.ConfigServiceImpl
import fr.o80.twitck.lib.internal.service.ExtensionProviderImpl
import fr.o80.twitck.lib.internal.service.TwitchApiImpl
import fr.o80.twitck.lib.internal.service.step.StepFormatter
import fr.o80.twitck.lib.internal.service.step.StepsExecutorImpl
import java.io.File

fun interface Installer<T> {
    fun install(
        pipeline: Pipeline,
        serviceLocator: ServiceLocator,
        configService: ConfigService
    ): T?
}

class BotFactory(
    private val configDirectory: File,
    private val oauthToken: String,
    private val hostName: String,
) {

    private val installers: MutableList<Installer<*>> = mutableListOf()

    init {
        check(configDirectory.isDirectory) { "The config path is NOT a directory!" }
    }

    fun install(installer: Installer<*>): BotFactory {
        installers += installer
        return this
    }

    fun create(): TwitckBot {
        val pipeline = PipelineImpl()
        val commandTriggering = CommandTriggeringImpl()
        val commandParser = CommandParser()
        val loggerFactory = Slf4jLoggerFactory()
        val configService: ConfigService = ConfigServiceImpl(configDirectory)
        val extensionProvider = ExtensionProviderImpl()
        val twitchApi = TwitchApiImpl(oauthToken, loggerFactory)

        val serviceLocator: ServiceLocator = ServiceLocatorImpl(
            extensionProvider = extensionProvider,
            loggerFactory = loggerFactory,
            twitchApi = twitchApi,
            commandTriggering = commandTriggering,
            commandParser = commandParser
        )

        installers.forEach { installer ->
            installer.install(pipeline, serviceLocator, configService)?.let {
                extensionProvider.register(it)
            }
        }

        val configuration = TwitckConfiguration(
            oauthToken = oauthToken,
            hostName = hostName,
            pipeline = pipeline,
            serviceLocator = serviceLocator,
            commandsFromExtension = commandTriggering
        )

        return TwitckBotImpl(configuration)
    }
}
