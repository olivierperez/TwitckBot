package fr.o80.twitck.lib.handler

import fr.o80.twitck.lib.bean.JoinEvent
import fr.o80.twitck.lib.bot.TwitckBot

typealias JoinHandler = (bot: TwitckBot, joinEvent: JoinEvent) -> JoinEvent

class JoinDispatcher(
    private val bot: TwitckBot,
    private val handlers: List<JoinHandler>
) {
    fun dispatch(join: JoinEvent) {
        handlers.fold(join) { acc, handler ->
            handler(bot, acc)
        }
    }
}
