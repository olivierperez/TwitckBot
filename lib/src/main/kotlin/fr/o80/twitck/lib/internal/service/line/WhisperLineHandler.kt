package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.WhisperEvent
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.internal.handler.WhisperCommandDispatcher
import fr.o80.twitck.lib.internal.handler.WhisperDispatcher

internal class WhisperLineHandler(
    private val messenger: Messenger,
    private val commandParser: CommandParser,
    private val whisperDispatcher: WhisperDispatcher,
    private val commandDispatcher: WhisperCommandDispatcher
) : LineHandler {

    private val regex =
        Regex("@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv WHISPER ([^ ]+) :(.+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val tags = matchResult.groupValues[1]
            val user = matchResult.groupValues[2]
            val destination = matchResult.groupValues[3]
            val message = matchResult.groupValues[4]

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

            val viewer = Viewer(
                login = user,
                displayName = displayName,
                badges = badges,
                userId = userId,
                color = color
            )
            val command = commandParser.parse(message)

            if (command != null) {
                commandDispatcher.dispatch(
                    CommandEvent(
                        messenger,
                        destination,
                        command,
                        bits = 0,
                        viewer
                    )
                )
            } else {
                whisperDispatcher.dispatch(
                    WhisperEvent(
                        destination = destination,
                        login = user,// TODO OPZ Passer le viewer
                        userId = userId,
                        message = message
                    )
                )
            }

        }
    }

}