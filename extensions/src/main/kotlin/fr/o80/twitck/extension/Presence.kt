package fr.o80.twitck.extension

import fr.o80.twitck.lib.ExtensionProvider
import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.extension.TwitckExtension

class Presence(
    private val channel: String
) {
    class Configuration {

        @DslMarker
        annotation class PresenceDsl

        private var channel: String? = null

        @PresenceDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        fun build(): Presence {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Presence::class.simpleName}")
            return Presence(channelName)
        }
    }

    companion object Extension : TwitckExtension<Configuration, Presence> {
        override fun install(pipeline: Pipeline, extensionProvider: ExtensionProvider, configure: Configuration.() -> Unit): Presence {
            return Configuration()
                .apply(configure)
                .build()
                .also { presence ->
                    pipeline.requestChannel(presence.channel)
                }
        }
    }
}