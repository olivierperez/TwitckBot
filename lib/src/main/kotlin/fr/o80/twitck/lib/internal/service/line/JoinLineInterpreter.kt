package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.JoinEvent
import fr.o80.twitck.lib.internal.handler.JoinDispatcher

internal class JoinLineInterpreter(
    private val bot: TwitckBot,
    private val dispatcher: JoinDispatcher
) : LineInterpreter {
    private val regex = Regex("^:([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv JOIN (#[^ ]+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val user = matchResult.groupValues[1]
            val channel = matchResult.groupValues[2]

            val viewer = Viewer(
                login = user,
                displayName = user,
                listOf(),
                "",
                ""
            )

            dispatcher.dispatch(
                JoinEvent(
                    bot = bot,
                    channel = channel,
                    viewer = viewer
                )
            )
        }
    }

}