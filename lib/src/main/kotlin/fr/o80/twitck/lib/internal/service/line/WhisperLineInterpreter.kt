package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.bean.event.WhisperEvent
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.internal.handler.WhisperCommandDispatcher
import fr.o80.twitck.lib.internal.handler.WhisperDispatcher

internal class WhisperLineInterpreter(
    private val messenger: Messenger,
    private val commandParser: CommandParser,
    private val whisperDispatcher: WhisperDispatcher,
    private val commandDispatcher: WhisperCommandDispatcher
) : LineInterpreter {

    private val regex =
        Regex("@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv WHISPER ([^ ]+) :(.+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val tags = Tags.from(matchResult.groupValues[1])
            val user = matchResult.groupValues[2]
            val destination = matchResult.groupValues[3]
            val message = matchResult.groupValues[4]

            val viewer = Viewer(
                login = user,
                displayName = tags.displayName,
                badges = tags.badges,
                userId = tags.userId,
                color = tags.color
            )
            val command = commandParser.parse(message)

            if (command != null) {
                commandDispatcher.dispatch(
                    CommandEvent(
                        messenger,
                        destination,
                        command,
                        bits = null,
                        viewer
                    )
                )
            } else {
                whisperDispatcher.dispatch(
                    WhisperEvent(
                        destination = destination,
                        viewer = viewer,
                        message = message
                    )
                )
            }

        }
    }

}