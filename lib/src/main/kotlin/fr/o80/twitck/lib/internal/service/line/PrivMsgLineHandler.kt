package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.internal.handler.MessageDispatcher

internal class PrivMsgLineHandler(
    private val bot: TwitckBot,
    private val dispatcher: MessageDispatcher
) : LineHandler {

    private val regex = Regex("^@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG (#[^ ]+) :(.+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val tags = matchResult.groupValues[1]
            val user = matchResult.groupValues[2]
            val channel = matchResult.groupValues[3]
            val msg = matchResult.groupValues[4]

            var badges = listOf<Badge>()
            var color = ""
            var displayName = ""
            var bits = 0

            tags.split(";").forEach {
                val (key, value) = it.split("=")
                when (key) {
                    "badges" -> badges = parseBadges(value)
                    "color" -> color = value
                    "bits" -> bits = value.toInt()
                    "display-name" -> displayName = value
                }
            }

            dispatcher.dispatch(
                MessageEvent(
                    bot = bot,
                    channel = channel,
                    login = user,
                    badges = badges,
                    message = msg
                )
            )
        }
    }

}
