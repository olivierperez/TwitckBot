package fr.o80.twitck.lib

import fr.o80.twitck.lib.bean.TwitckConfiguration
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.bot.TwitckBotImpl
import fr.o80.twitck.lib.extension.TwitckExtension

@DslMarker
annotation class TwitckConfigDsl

@TwitckConfigDsl
fun twitckBot(
    oauthToken: String,
    configurator: TwitckConfigurator.() -> Unit
): TwitckBot {
    val configuration = TwitckConfigurator().apply(configurator).build()
    return TwitckBotImpl(oauthToken, configuration)
}

class TwitckConfigurator {
    private val extensions: MutableList<Any> = mutableListOf()
    private val pipeline = PipelineImpl()

    private val extensionProvider = object : ExtensionProvider {
        override fun <T> provide(extensionClass: Class<T>): T? =
            extensions
                .firstOrNull { extension -> extensionClass.isAssignableFrom(extension::class.java) }
                ?.let { extension -> extensionClass.cast(extension) }
    }

    @TwitckConfigDsl
    fun <Configuration : Any, A : Any> install(
        extension: TwitckExtension<Configuration, A>,
        configure: Configuration.() -> Unit
    ) {
        val installed = extension.install(pipeline, extensionProvider, configure)
        extensions += installed
    }

    fun build(): TwitckConfiguration {
        return TwitckConfiguration(
            requestedChannels = pipeline.requestedChannels,
            joinHandlers = pipeline.joinHandlers,
            messageHandlers = pipeline.messageHandlers,
            whisperHandlers = pipeline.whisperHandlers
        )
    }

}

// TODO Sortir ailleurs
// TODO Provider plusieurs extensions à partir d'une interface passée en params (+ créer un module qui liste les interfaces disponibles ? pour le cas où quelqu'un veuille développer sa propre extension Help)
interface ExtensionProvider {
    fun <T> provide(extensionClass: Class<T>): T?
}