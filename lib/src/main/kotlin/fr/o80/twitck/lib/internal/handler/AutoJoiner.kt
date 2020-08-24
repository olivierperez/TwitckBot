package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.service.log.LoggerFactory
import fr.o80.twitck.lib.internal.TwitckBotImpl

internal class AutoJoiner(
    private val bot: TwitckBotImpl,
    private val requestedChannels: Iterable<String>,
    loggerFactory: LoggerFactory
) {
    private val logger = loggerFactory.getLogger(AutoJoiner::class)

    fun join() {
        logger.info("Automatically joining channels $requestedChannels")
        requestedChannels.forEach(bot::join)
    }
}