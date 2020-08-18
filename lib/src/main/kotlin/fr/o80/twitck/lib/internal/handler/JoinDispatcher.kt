package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.handler.JoinHandler

internal class JoinDispatcher(
    private val bot: TwitckBot,
    private val handlers: List<JoinHandler>
) {
    fun dispatch(join: JoinEvent) {
        handlers.fold(join) { acc, handler ->
            handler(bot, acc)
        }
    }
}
