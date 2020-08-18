package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.internal.TwitckBotImpl

internal class AutoJoiner(
    private val bot: TwitckBotImpl,
    private val requestedChannels: Iterable<String>
) {
    fun join() {
        println("Automatically joining channels $requestedChannels")
        requestedChannels.forEach(bot::join)
    }
}