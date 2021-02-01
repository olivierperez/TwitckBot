package fr.o80.twitck.extension.channel

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import fr.o80.twitck.extension.channel.config.ChannelConfiguration
import fr.o80.twitck.extension.channel.config.CommandStep
import fr.o80.twitck.extension.channel.config.MessageStep
import fr.o80.twitck.extension.channel.config.OverlayStep
import fr.o80.twitck.extension.channel.config.SoundStep
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

            val stepsExecutor = StepsExecutor(serviceLocator.extensionProvider)
            val commands = ChannelCommands(config.commands, stepsExecutor)

            return Channel().also {
                pipeline.requestChannel(config.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
//                pipeline.interceptFollowEvent(channel::interceptFollowEvent)
//                pipeline.interceptJoinEvent(channel::interceptJoinEvent)
//                pipeline.interceptSubscriptionEvent(channel::interceptSubscriptionEvent)
            }
        }

        private fun channelConfigMoshi() = Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(CommandStep::class.java, "type")
                    .withSubtype(SoundStep::class.java, CommandStep.Type.SOUND.value)
                    .withSubtype(OverlayStep::class.java, CommandStep.Type.OVERLAY.value)
                    .withSubtype(MessageStep::class.java, CommandStep.Type.MESSAGE.value)
            )
            .build()
    }
}
