package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.WhisperEvent
import fr.o80.twitck.lib.internal.handler.WhisperDispatcher

internal class WhisperLineHandler(
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
            var userId = ""
            var color = ""
            var displayName = ""

            tags.split(";").forEach {
                val (key, value) = it.split("=")
                when (key) {
                    "badges" -> badges = parseBadges(value)
                    "user-id" -> userId = value
                    "color" -> color = value
                    "display-name" -> displayName = value
                }
            }

            dispatcher.dispatch(
                WhisperEvent(
                    destination = destination,
                    login = user,
                    userId = userId,
                    message = msg
                )
            )

        }
    }

}