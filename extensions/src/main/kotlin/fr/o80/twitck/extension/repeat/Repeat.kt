package fr.o80.twitck.extension.repeat

import fr.o80.twitck.extension.points.Points
import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Deadline
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.bean.SendMessage
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger

class Repeat(
    val channel: String,
    val messages: MutableList<String>,
    val logger: Logger
) {

    private var configured = false

    fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        if (configured) return messageEvent
        configured = true
        recordRepeatedMessage(messenger)
        return messageEvent
    }

    private fun recordRepeatedMessage(messenger: Messenger) {
        logger.info("Recording ${messages.size} repeated messages...")
        messages.forEach { message ->
            messenger.send(SendMessage(channel, message, Deadline.Repeated))
        }
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private val messages = mutableListOf<String>()

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun remind(message: String) {
            messages += message
        }

        fun build(serviceLocator: ServiceLocator): Repeat {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Points::class.simpleName}")
            val logger = serviceLocator.loggerFactory.getLogger(Repeat::class)

            return Repeat(
                channelName,
                messages,
                logger
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
                .build(serviceLocator)
                .also { repeat ->
                    pipeline.interceptMessageEvent(repeat::interceptMessageEvent)
                    pipeline.requestChannel(repeat.channel)
                }
        }
    }
}
