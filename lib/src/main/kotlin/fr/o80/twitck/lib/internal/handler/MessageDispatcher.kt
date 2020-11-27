package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.service.Messenger

internal class MessageDispatcher(
    private val messenger: Messenger,
    private val handlers: List<MessageHandler>
) {
    fun dispatch(messageEvent: MessageEvent) {
        handlers.fold(messageEvent) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
