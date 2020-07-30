package fr.o80.twitck.lib.service.line

import fr.o80.twitck.lib.bean.Badge
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.handler.WhisperDispatcher
import fr.o80.twitck.lib.handler.WhisperEvent

class WhisperLineHandler(
    private val bot: TwitckBot,
    private val dispatcher: WhisperDispatcher
) : LineHandler {

    private val regex = Regex("@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv WHISPER ([^ ]+) :(.+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val tags = matchResult.groupValues[1]
            val user = matchResult.groupValues[2]
            val destination = matchResult.groupValues[3]
            val msg = matchResult.groupValues[4]

            var badges = listOf<Badge>()
            var color = ""
            var displayName = ""

            tags.split(";").forEach {
                val (key, value) = it.split("=")
                when (key) {
                    "badges" -> badges = parseBadges(value)
                    "color" -> color = value
                    "display-name" -> displayName = value
                }
            }

            dispatcher.dispatch(
                WhisperEvent(
                    destination = destination,
                    login = user,
                    message = msg
                )
            )

        }
    }

}