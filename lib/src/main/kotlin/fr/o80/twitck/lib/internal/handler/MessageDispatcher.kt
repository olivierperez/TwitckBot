package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.Messenger
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.handler.MessageHandler

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
