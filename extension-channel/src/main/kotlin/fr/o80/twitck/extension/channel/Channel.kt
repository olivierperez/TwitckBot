package fr.o80.twitck.extension.channel

import fr.o80.twitck.extension.channel.config.ChannelConfiguration
import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.ConfigService

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

            val logger = serviceLocator.loggerFactory.getLogger(Channel::class)

            val commands = ChannelCommands(config.data.commands, serviceLocator.stepsExecutor)
            val follows = ChannelFollows(config.data.follows, serviceLocator.stepsExecutor)
            val bits = ChannelBits(config.data.bits, serviceLocator.stepsExecutor, logger)

            return Channel().also {
                pipeline.requestChannel(config.data.channel.name)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
                pipeline.interceptFollowEvent(follows::interceptFollowEvent)
                pipeline.interceptBitsEvent(bits::interceptBitsEvent)
//                pipeline.interceptJoinEvent(channel::interceptJoinEvent)
//                pipeline.interceptSubscriptionEvent(channel::interceptSubscriptionEvent)
            }
        }
    }
}
