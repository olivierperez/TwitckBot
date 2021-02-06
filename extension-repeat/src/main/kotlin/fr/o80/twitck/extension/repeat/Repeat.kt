package fr.o80.twitck.extension.repeat

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService
import java.time.Duration

class Repeat(
    private val channel: String,
    private val intervalBetweenRepeatedMessages: Duration,
    private val messages: List<String>
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

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Repeat? {
            val config = configService.getConfig("repeat.json", RepeatConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            serviceLocator.loggerFactory.getLogger(Repeat::class)
                .info("Installing Repeat extension...")

            return Repeat(
                config.data.channel,
                Duration.ofSeconds(config.data.secondsBetweenRepeatedMessages),
                config.data.messages
            ).also { repeat ->
                pipeline.interceptMessageEvent(repeat::interceptMessageEvent)
                pipeline.requestChannel(repeat.channel)
            }
        }
    }

}
