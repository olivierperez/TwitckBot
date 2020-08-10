package fr.o80.twitck.lib.service

import fr.o80.twitck.lib.bean.Command
import fr.o80.twitck.lib.bean.MessageEvent
import java.util.Locale

class CommandParser {
    fun parse(messageEvent: MessageEvent): Command {
        val split = messageEvent.message.split(" ")
        val tag = split[0].toLowerCase(Locale.FRENCH)
        return if (split.size == 1) {
            Command(messageEvent.badges, tag)
        } else {
            Command(
                messageEvent.badges,
                tag,
                split.subList(1, split.size)
            )
        }
    }
}