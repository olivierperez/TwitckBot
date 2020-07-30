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

    @TwitckConfigDsl
    fun <Configuration : Any, A : Any> install(
        extension: TwitckExtension<Configuration, A>,
        configure: Configuration.() -> Unit
    ) {
        val installed = extension.install(pipeline, configure)
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
