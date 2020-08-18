package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.TwitckBot

internal class MessageDispatcher(
    private val bot: TwitckBot,
    private val handlers: List<MessageHandler>
) {
    fun dispatch(messageEvent: MessageEvent) {
        handlers.fold(messageEvent) { acc, handler ->
            handler(bot, acc)
        }
    }
}
