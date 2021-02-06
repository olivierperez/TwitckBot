package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.bean.Command
import java.util.*

class CommandParser {

    private val regex: Regex = "![a-zA-Z0-9_]+.*".toRegex()

    fun parse(message: String): Command? {
        if (!message.matches(regex)) return null

        val split = message.split(" ")
        val tag = split[0].toLowerCase(Locale.FRENCH)
        return if (split.size == 1) {
            Command(
                tag = tag
            )
        } else {
            Command(
                tag = tag,
                options = split.subList(1, split.size)
            )
        }
    }

}
