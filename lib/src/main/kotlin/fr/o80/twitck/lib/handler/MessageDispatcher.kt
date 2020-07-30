package fr.o80.twitck.lib.handler

import fr.o80.twitck.lib.bean.MessageEvent
import fr.o80.twitck.lib.bot.TwitckBot

typealias MessageHandler = (bot: TwitckBot, messageEvent: MessageEvent) -> MessageEvent

class MessageDispatcher(
    private val bot: TwitckBot,
    private val handlers: List<MessageHandler>
) {
    fun dispatch(messageEvent: MessageEvent) {
        handlers.fold(messageEvent) { acc, handler ->
            handler(bot, acc)
        }
    }
}
