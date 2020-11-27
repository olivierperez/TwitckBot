package fr.o80.twitck.extension.repeat

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import java.time.Duration

class Repeat(
    private val channel: String,
    private val intervalBetweenRepeatedMessages: Duration,
    private val messages: MutableList<String>
) {

    private var configured = false

    private var interrupted = false

    fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        if (configured) return messageEvent

        configured = true
        startLoop(messenger)
        return messageEvent
    }

    private fun startLoop(messenger: Messenger) {
        Thread {
            while (!interrupted) {
                Thread.sleep(intervalBetweenRepeatedMessages.toMillis())
                messages.randomOrNull()?.let { message ->
                    messenger.sendImmediately(channel, message)
                }
            }
        }.start()
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private var intervalBetweenRepeatedMessages: Duration = Duration.ofMinutes(5)

        private val messages = mutableListOf<String>()

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun interval(interval: Duration) {
            intervalBetweenRepeatedMessages = interval
        }

        @Dsl
        fun remind(message: String) {
            messages += message
        }

        fun build(): Repeat {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Repeat::class.simpleName}")

            return Repeat(
                channelName,
                intervalBetweenRepeatedMessages,
                messages
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Repeat> {

        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Repeat {
            return Configuration()
                .apply(configure)
                .build()
                .also { repeat ->
                    pipeline.interceptMessageEvent(repeat::interceptMessageEvent)
                    pipeline.requestChannel(repeat.channel)
                }
        }
    }
}
