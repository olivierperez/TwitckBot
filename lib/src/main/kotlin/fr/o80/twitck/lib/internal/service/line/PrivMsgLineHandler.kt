package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.internal.handler.CommandDispatcher
import fr.o80.twitck.lib.internal.handler.MessageDispatcher

internal class PrivMsgLineHandler(
    private val messenger: Messenger,
    private val commandParser: CommandParser,
    private val messageDispatcher: MessageDispatcher,
    private val commandDispatcher: CommandDispatcher
) : LineHandler {

    private val regex = Regex("^@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG (#[^ ]+) :(.+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val tags = matchResult.groupValues[1]
            val user = matchResult.groupValues[2]
            val channel = matchResult.groupValues[3]
            val msg = matchResult.groupValues[4]

            var badges = listOf<Badge>()
            var userId = ""
            var color = ""
            var displayName = ""
            var bits = 0

            tags.split(";").forEach {
                val (key, value) = it.split("=")
                when (key) {
                    "badges" -> badges = parseBadges(value)
                    "user-id" -> userId = value
                    "color" -> color = value
                    "bits" -> bits = value.toInt()
                    "display-name" -> displayName = value
                }
            }

            val command = commandParser.parse(msg)
            if (command != null) {
                commandDispatcher.dispatch(CommandEvent(messenger, channel, user, userId, badges, command))
            } else {
                messageDispatcher.dispatch(MessageEvent(messenger, channel, user, userId, badges, msg))
            }
        }
    }

}
