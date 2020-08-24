package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.WhisperEvent
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger

/**
 * This extension provides a way to react to whispers to the bot.
 */
class Whisper(
    private val whisperCallbacks: Iterable<WhisperCallback>,
    private val logger: Logger
) {

    fun interceptWhisperEvent(bot: TwitckBot, whisperEvent: WhisperEvent) {
        logger.debug("I've just seen a whisper event: ${whisperEvent.destination} > ${whisperEvent.message}")

        whisperCallbacks.forEach { callback ->
            callback(bot, whisperEvent)
        }
    }

    class Configuration {

        @DslMarker
        annotation class ChannelDsl

        private val whisperCallbacks: MutableList<WhisperCallback> = mutableListOf()

        @ChannelDsl
        fun whisper(callback: WhisperCallback) {
            whisperCallbacks += callback
        }

        fun build(serviceLocator: ServiceLocator): Whisper {
            return Whisper(
                whisperCallbacks = whisperCallbacks,
                logger = serviceLocator.loggerFactory.getLogger(Whisper::class)
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Whisper> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Whisper {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { channel ->
                    pipeline.interceptWhisperEvent(channel::interceptWhisperEvent)
                }
        }
    }
}

typealias WhisperCallback = (
    bot: TwitckBot,
    whisper: WhisperEvent
) -> Unit
