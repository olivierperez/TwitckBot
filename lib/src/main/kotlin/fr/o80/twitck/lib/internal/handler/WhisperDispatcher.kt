package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.WhisperEvent
import fr.o80.twitck.lib.api.handler.WhisperHandler
import fr.o80.twitck.lib.api.TwitckBot

internal class WhisperDispatcher(
    private val bot: TwitckBot,
    private val handlers: List<WhisperHandler>
) {
    fun dispatch(event: WhisperEvent) {
        handlers.forEach { handler ->
            handler(bot, event)
        }
    }
}

