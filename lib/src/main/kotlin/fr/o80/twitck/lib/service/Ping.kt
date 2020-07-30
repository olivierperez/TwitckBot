package fr.o80.twitck.lib.service

import fr.o80.twitck.lib.bot.TwitckBot

class Ping(private val bot: TwitckBot) {
    fun handleLine(line: String) {
        if (line == "PING :tmi.twitch.tv") {
            bot.sendLine("PONG :tmi.twitch.tv")
        }
    }
}