package fr.o80.twitck.extension.actions

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator

class WebSocketRemoteActions(
    uiWebSocket: UiWebSocket
) {

    init {
        uiWebSocket.start()
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        internal fun build(serviceLocator: ServiceLocator): WebSocketRemoteActions {
            val logger = serviceLocator.loggerFactory.getLogger(WebSocketRemoteActions::class)
            val store = RemoteActionStore()
            val webSocket = UiWebSocket(
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
        }
    }

}
