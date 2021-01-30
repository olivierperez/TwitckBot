package fr.o80.twitck.extension.actions

import fr.o80.slobs.AsyncSlobsClient
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator

class WebSocketRemoteActions(
    private val uiWebSocket: UiWebSocket
) {

    init {
        uiWebSocket.start()
    }

    private fun interceptMessageEvent(
        messenger: Messenger,
        messageEvent: MessageEvent
    ): MessageEvent {
        uiWebSocket.messenger = messenger
        return messageEvent
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private var slobsHost: String? = null
        private var slobsPort: Int? = null
        private var slobsToken: String? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun slobs(host: String, port: Int, token: String) {
            this.slobsHost = host
            this.slobsPort = port
            this.slobsToken = token
        }

        internal fun build(serviceLocator: ServiceLocator): WebSocketRemoteActions {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${WebSocketRemoteActions::class.simpleName}")

            val logger = serviceLocator.loggerFactory.getLogger(WebSocketRemoteActions::class)
            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val store = RemoteActionStore(storage)

            val slobsClient = AsyncSlobsClient(
                slobsHost ?: throw IllegalStateException("Slobs host is required"),
                slobsPort ?: throw IllegalStateException("Slobs port is required"),
                slobsToken ?: throw IllegalStateException("Slobs token is required")
            )

            val webSocket = UiWebSocket(
                channelName,
                store,
                slobsClient,
                serviceLocator.commandTriggering,
                logger
            )
            return WebSocketRemoteActions(webSocket)
        }
    }

    /*companion object : ExtensionInstaller<Configuration, WebSocketRemoteActions> {
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
    }*/

}
