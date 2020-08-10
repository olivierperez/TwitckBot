package fr.o80.twitck.extension

import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.extension.TwitckExtension
import fr.o80.twitck.lib.handler.WhisperEvent
import fr.o80.twitck.lib.service.ServiceLocator

/**
 * This extension provides a way to react to whispers to the bot.
 */
class Whisper(
    private val whisperCallbacks: Iterable<WhisperCallback>
) {

    fun interceptWhisperEvent(bot: TwitckBot, whisperEvent: WhisperEvent) {
        println("> I've just seen a whisper event: ${whisperEvent.destination} > ${whisperEvent.message}")

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

        fun build(): Whisper {
            return Whisper(
                whisperCallbacks = whisperCallbacks
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
                .build()
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
