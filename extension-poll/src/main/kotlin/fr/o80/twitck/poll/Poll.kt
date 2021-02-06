package fr.o80.twitck.poll

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class Poll {

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Poll? {
            val config = configService.getConfig("poll.json", PollConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            serviceLocator.loggerFactory.getLogger(Poll::class)
                .info("Installing Poll extension...")

            val points = serviceLocator.extensionProvider.firstOrNull(PointsExtension::class)

            val commands = PollCommands(
                channel = config.data.channel,
                privilegedBadges = config.data.privilegedBadges,
                i18n = config.data.i18n,
                pointsForEachVote = config.data.pointsEarnPerVote,
                points = points
            )

            return Poll().also {
                pipeline.requestChannel(config.data.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
            }
        }
    }

}