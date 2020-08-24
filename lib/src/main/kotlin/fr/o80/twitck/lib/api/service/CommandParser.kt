package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import java.util.Locale

class CommandParser {
    fun parse(messageEvent: MessageEvent): Command? {
        if (!messageEvent.message.matches("![a-zA-Z0-9_]+.*".toRegex())) return null

        val split = messageEvent.message.split(" ")
        val tag = split[0].toLowerCase(Locale.FRENCH)
        return if (split.size == 1) {
            Command(
                login = messageEvent.login,
                badges = messageEvent.badges,
                tag = tag)
        } else {
            Command(
                login = messageEvent.login,
                badges = messageEvent.badges,
                tag = tag,
                options = split.subList(1, split.size)
            )
        }
    }
}