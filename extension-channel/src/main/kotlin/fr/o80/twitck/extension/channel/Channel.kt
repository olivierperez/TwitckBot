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
        ): Channel? {
            val config = configService.getConfig(
                "channel.json",
                ChannelConfiguration::class
            ) ?: return null

            val commands = ChannelCommands(config.data.commands, serviceLocator.stepsExecutor)
            val follows = ChannelFollows(config.data.follows, serviceLocator.stepsExecutor)

            return Channel().also {
                pipeline.requestChannel(config.data.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
                pipeline.interceptFollowEvent(follows::interceptFollowEvent)
//                pipeline.interceptJoinEvent(channel::interceptJoinEvent)
//                pipeline.interceptSubscriptionEvent(channel::interceptSubscriptionEvent)
            }
        }
    }
}
