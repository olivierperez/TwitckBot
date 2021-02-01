package fr.o80.twitck.poll

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class Poll {

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Poll {
            val config = configService.getConfig("poll.json", PollConfiguration::class)

            val commands = PollCommands(
                channel = config.channel,
                privilegedBadges = config.privilegedBadges,
                i18n = config.i18n,
                pointsForEachVote = config.pointsEarnPerVote,
                extensionProvider = serviceLocator.extensionProvider
            )

            return Poll().also {
                pipeline.requestChannel(config.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
            }
        }
    }

}