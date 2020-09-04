package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.bean.TwitckConfiguration
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Slf4jLoggerFactory
import fr.o80.twitck.lib.internal.PipelineImpl
import fr.o80.twitck.lib.internal.TwitckBotImpl
import fr.o80.twitck.lib.internal.service.TwitchApiImpl
import kotlin.reflect.KClass
import kotlin.reflect.cast

@DslMarker
private annotation class TwitckConfigDsl

@TwitckConfigDsl
fun twitckBot(
    oauthToken: String,
    clientId: String,
    configurator: TwitckConfigurator.() -> Unit
): TwitckBot {
    val configuration = TwitckConfigurator(oauthToken, clientId).apply(configurator).build()
    return TwitckBotImpl(configuration)
}

class TwitckConfigurator(
    private val oauthToken: String,
    clientId: String
) {
    private val extensions: MutableList<Any> = mutableListOf()
    private val pipeline = PipelineImpl()
    private val loggerFactory = Slf4jLoggerFactory()

    private val serviceLocator: ServiceLocator = ServiceLocator(
        extensionProvider = object : ExtensionProvider {
            override fun <T : Any> provide(extensionInterface: KClass<T>): List<T> =
                extensions
                    .filter { extension -> extensionInterface.isInstance(extension) }
                    .map { extension -> extensionInterface.cast(extension) }

            override fun <T : Any> forEach(extensionInterface: KClass<T>, block: (extension: T) -> Unit) {
                provide(extensionInterface)
                    .forEach(block)
            }
        },
        loggerFactory = loggerFactory,
        twitchApi = TwitchApiImpl(clientId, oauthToken, loggerFactory)
    )

    @TwitckConfigDsl
    fun <Configuration : Any, A : Any> install(
        extension: TwitckExtension<Configuration, A>,
        configure: Configuration.() -> Unit
    ) {
        val installed = extension.install(pipeline, serviceLocator, configure)
        extensions += installed
    }

    fun build(): TwitckConfiguration {
        return TwitckConfiguration(
            oauthToken = oauthToken,
            requestedChannels = pipeline.requestedChannels,
            joinHandlers = pipeline.joinHandlers,
            messageHandlers = pipeline.messageHandlers,
            commandHandlers = pipeline.commandHandlers,
            whisperHandlers = pipeline.whisperHandlers,
            loggerFactory = loggerFactory,
            commandParser = serviceLocator.commandParser
        )
    }

}
