package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.exception.ExtensionDependencyException
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class Market(
    extensionProvider: ExtensionProvider
) {

    init {
        extensionProvider.forEach(HelpExtension::class) { helper ->
            helper.registerCommand("!buy")
            helper.registerCommand("!market")
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

            val commands = MarketCommands(
                config.data.channel,
                config.data.i18n,
                config.data.products,
                logger,
                points,
                serviceLocator.stepsExecutor
            )

            return Market(
                serviceLocator.extensionProvider
            ).also {
                pipeline.requestChannel(config.data.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
            }
        }
    }

}
