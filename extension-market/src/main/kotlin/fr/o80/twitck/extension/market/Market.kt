package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.exception.ExtensionDependencyException
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.service.ConfigService
import fr.o80.twitck.lib.api.service.ServiceLocator

class Market(
    help: HelpExtension?
) {

    init {
        help?.run {
            registerCommand("!buy")
            registerCommand("!market")
        }
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Market? {
            val config = configService.getConfig("market.json", MarketConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(Market::class)
            logger.info("Installing Market extension...")

            val points = serviceLocator.extensionProvider.firstOrNull(PointsExtension::class)
                ?: throw ExtensionDependencyException("Market", "Points")
            val help = serviceLocator.extensionProvider.firstOrNull(HelpExtension::class)

            val commands = MarketCommands(
                config.data.channel.name,
                config.data.i18n,
                config.data.products,
                points,
                serviceLocator.stepsExecutor,
                logger
            )
            val rewards = MarketRewards(
                config.data.rewards,
                serviceLocator.stepsExecutor,
                logger
            )

            return Market(
                help
            ).also {
                pipeline.requestChannel(config.data.channel.name)
                pipeline.interceptCommandEvent { messenger, commandEvent ->
                    commands.interceptCommandEvent(messenger, commandEvent)
                }
                pipeline.interceptRewardEvent { messenger, rewardEvent ->
                    rewards.interceptRewardEvent(messenger, rewardEvent)
                }
            }
        }
    }

}
