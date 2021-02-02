package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelpExtension
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
        ): Market {
            val config = configService.getConfig("market.json", MarketConfiguration::class)
            val logger = serviceLocator.loggerFactory.getLogger(Market::class)

            val commands = MarketCommands(
                config.channel,
                config.i18n,
                config.products,
                logger,
                serviceLocator.extensionProvider,
                serviceLocator.stepsExecutor
            )

            return Market(
                serviceLocator.extensionProvider
            ).also {
                pipeline.requestChannel(config.channel)
                pipeline.interceptCommandEvent(commands::interceptCommandEvent)
            }
        }
    }

}
