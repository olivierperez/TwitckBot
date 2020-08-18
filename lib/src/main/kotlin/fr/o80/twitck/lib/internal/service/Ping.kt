package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.TwitckBot

internal class Ping(private val bot: TwitckBot) {
    fun handleLine(line: String) {
        if (line == "PING :tmi.twitch.tv") {
            bot.sendLine("PONG :tmi.twitch.tv")
        }
    }
}