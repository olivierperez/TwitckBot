package fr.o80.twitck.lib.handler

import fr.o80.twitck.lib.bot.TwitckBotImpl

class AutoJoiner(
    private val bot: TwitckBotImpl,
    private val requestedChannels: Iterable<String>
) {
    fun join() {
        println("Automatically joining channels $requestedChannels")
        requestedChannels.forEach(bot::join)
    }
}