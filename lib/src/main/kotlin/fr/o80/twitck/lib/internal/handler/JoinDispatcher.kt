package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.Messenger
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.handler.JoinHandler

internal class JoinDispatcher(
    private val messenger: Messenger,
    private val handlers: List<JoinHandler>
) {
    fun dispatch(join: JoinEvent) {
        handlers.fold(join) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
