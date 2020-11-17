package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.handler.CommandHandler
import fr.o80.twitck.lib.api.service.Messenger

internal class WhisperCommandDispatcher(
    private val messenger: Messenger,
    private val handlers: List<CommandHandler>
) {
    fun dispatch(commandEvent: CommandEvent) {
        handlers.fold(commandEvent) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
