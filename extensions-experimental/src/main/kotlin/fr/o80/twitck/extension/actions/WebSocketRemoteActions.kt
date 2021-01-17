package fr.o80.twitck.extension.actions

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator

class WebSocketRemoteActions(
    private val uiWebSocket: UiWebSocket
) {

    init {
        uiWebSocket.start()
    }

    private fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        uiWebSocket.messenger = messenger
        return messageEvent
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        internal fun build(serviceLocator: ServiceLocator): WebSocketRemoteActions {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${WebSocketRemoteActions::class.simpleName}")

            val logger = serviceLocator.loggerFactory.getLogger(WebSocketRemoteActions::class)
            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val store = RemoteActionStore(storage)

            val webSocket = UiWebSocket(
                channelName,
                store,
                serviceLocator.commandTriggering,
                logger
            )
            return WebSocketRemoteActions(webSocket)
        }
    }

    companion object : TwitckExtension<Configuration, WebSocketRemoteActions> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): WebSocketRemoteActions {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { webSocketRemoteActions ->
                    pipeline.interceptMessageEvent { messenger, messageEvent ->
                        webSocketRemoteActions.interceptMessageEvent(messenger, messageEvent)
                    }
                }
        }
    }

}
