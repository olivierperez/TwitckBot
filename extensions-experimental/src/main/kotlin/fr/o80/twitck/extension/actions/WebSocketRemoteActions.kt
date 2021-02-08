package fr.o80.twitck.extension.actions

import fr.o80.slobs.AsyncSlobsClient
import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.exception.ExtensionDependencyException
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
        ): WebSocketRemoteActions? {
            val config = configService.getConfig(
                "remote_actions.json",
                WebSocketRemoteActionsConfiguration::class
            )
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(WebSocketRemoteActions::class)
            logger.info("Installing RemoteActions extension...")

            val storage = serviceLocator.extensionProvider.firstOrNull(StorageExtension::class)
                ?: throw ExtensionDependencyException("RemoteActions", "Storage")

            val store = RemoteActionStore(storage)

            val slobsClient = AsyncSlobsClient(
                config.data.slobsHost,
                config.data.slobsPort,
                config.data.slobsToken
            )

            val webSocket = UiWebSocket(
                config.data.channel,
                config.data.actionsPort,
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
