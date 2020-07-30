package fr.o80.twitck.lib.service.line

import fr.o80.twitck.lib.bean.JoinEvent
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.handler.JoinDispatcher

class JoinLineHandler(
    private val bot: TwitckBot,
    private val dispatcher: JoinDispatcher
) : LineHandler {
    private val regex = Regex("^:([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv JOIN (#[^ ]+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val user = matchResult.groupValues[1]
            val channel = matchResult.groupValues[2]

            dispatcher.dispatch(
                JoinEvent(
                    bot = bot,
                    channel = channel,
                    login = user
                )
            )
        }
    }

}