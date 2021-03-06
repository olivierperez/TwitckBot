package fr.o80.twitck.extension.whisper

import fr.o80.twitck.lib.api.bean.event.WhisperEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger

/**
 * This extension provides a way to react to whispers to the bot.
 */
class Whisper(
    private val whisperCallbacks: Iterable<WhisperCallback>,
    private val logger: Logger
) {

    fun interceptWhisperEvent(messenger: Messenger, whisperEvent: WhisperEvent) {
        logger.trace("I've just seen a whisper event: ${whisperEvent.destination} > ${whisperEvent.message}")

        whisperCallbacks.forEach { callback ->
            callback(messenger, whisperEvent)
        }
    }

    class Configuration {

        @DslMarker
        private annotation class ChannelDsl

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

    /*companion object Extension : ExtensionInstaller<Configuration, Whisper> {
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
    }*/
}

typealias WhisperCallback = (
    messenger: Messenger,
    whisper: WhisperEvent
) -> Unit
