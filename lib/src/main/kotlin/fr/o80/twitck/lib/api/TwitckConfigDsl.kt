package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.bean.TwitckConfiguration
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.PipelineImpl
import fr.o80.twitck.lib.internal.TwitckBotImpl
import fr.o80.twitck.lib.internal.service.TwitchApiImpl

@DslMarker
annotation class TwitckConfigDsl

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

    private val serviceLocator: ServiceLocator = ServiceLocator(
        extensionProvider = object : ExtensionProvider {
            override fun <T> provide(extensionInterface: Class<T>): List<T> =
                extensions
                    .filter { extension -> extensionInterface.isAssignableFrom(extension::class.java) }
                    .map { extension -> extensionInterface.cast(extension) }
        },
        twitchApi = TwitchApiImpl(clientId)
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
            whisperHandlers = pipeline.whisperHandlers
        )
    }

}
