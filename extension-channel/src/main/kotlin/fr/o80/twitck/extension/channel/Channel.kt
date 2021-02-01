package fr.o80.twitck.extension.channel

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
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
                ChannelConfiguration::class,
                channelConfigMoshi()
            )

            val commands = ChannelCommands(config.commands, serviceLocator.extensionProvider)

            return Channel().also {
                pipeline.requestChannel(config.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
//                pipeline.interceptJoinEvent(channel::interceptJoinEvent)
//                pipeline.interceptFollowEvent(channel::interceptFollowEvent)
//                pipeline.interceptSubscriptionEvent(channel::interceptSubscriptionEvent)
            }
        }

        private fun channelConfigMoshi() = Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(CommandStep::class.java, "type")
                    .withSubtype(SoundStep::class.java, CommandStepType.SOUND.value)
                    .withSubtype(OverlayStep::class.java, CommandStepType.OVERLAY.value)
            )
            .build()
    }
}
