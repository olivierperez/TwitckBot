package fr.o80.twitck.extension.actions

import fr.o80.slobs.AsyncSlobsClient
import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

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

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): WebSocketRemoteActions {
            val config = configService.getConfig("remote_actions.json", WebSocketRemoteActionsConfiguration::class)

            val logger = serviceLocator.loggerFactory.getLogger(WebSocketRemoteActions::class)
            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val store = RemoteActionStore(storage)

            val slobsClient = AsyncSlobsClient(
               config.slobsHost,
               config.slobsPort,
               config.slobsToken
            )

            val webSocket = UiWebSocket(
                config.channel,
                store,
                slobsClient,
                serviceLocator.commandTriggering,
                logger
            )
            return WebSocketRemoteActions(webSocket).also { webSocketRemoteActions ->
                pipeline.interceptMessageEvent { messenger, messageEvent ->
                    webSocketRemoteActions.interceptMessageEvent(messenger, messageEvent)
                }
            }
        }
    }

}
