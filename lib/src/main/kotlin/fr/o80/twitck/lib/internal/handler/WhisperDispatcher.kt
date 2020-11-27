package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.event.WhisperEvent
import fr.o80.twitck.lib.api.handler.WhisperHandler
import fr.o80.twitck.lib.api.service.Messenger

internal class WhisperDispatcher(
    private val messenger: Messenger,
    private val handlers: List<WhisperHandler>
) {
    fun dispatch(event: WhisperEvent) {
        handlers.forEach { handler ->
            handler(messenger, event)
        }
    }
}
