package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.Messenger
import fr.o80.twitck.lib.api.bean.WhisperEvent
import fr.o80.twitck.lib.api.handler.WhisperHandler

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
