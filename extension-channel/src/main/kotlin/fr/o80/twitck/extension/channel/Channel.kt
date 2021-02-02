package fr.o80.twitck.extension.channel

import fr.o80.twitck.extension.channel.config.ChannelConfiguration
import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class Channel {

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Channel {
            val config = configService.getConfig(
                "channel.json",
                ChannelConfiguration::class
            )

            val commands = ChannelCommands(config.commands, serviceLocator.stepsExecutor)
            val follows = ChannelFollows(config.follows, serviceLocator.stepsExecutor)

            return Channel().also {
                pipeline.requestChannel(config.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
                pipeline.interceptFollowEvent(follows::interceptFollowEvent)
//                pipeline.interceptJoinEvent(channel::interceptJoinEvent)
//                pipeline.interceptSubscriptionEvent(channel::interceptSubscriptionEvent)
            }
        }
    }
}
