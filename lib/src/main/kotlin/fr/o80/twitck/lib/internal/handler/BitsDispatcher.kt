package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.event.BitsEvent
import fr.o80.twitck.lib.api.handler.BitsHandler
import fr.o80.twitck.lib.api.service.Messenger

internal class BitsDispatcher(
    private val messenger: Messenger,
    private val handlers: List<BitsHandler>
) {
    fun dispatch(bitsEvent: BitsEvent) {
        handlers.fold(bitsEvent) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
