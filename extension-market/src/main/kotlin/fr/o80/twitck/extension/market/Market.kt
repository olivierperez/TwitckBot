package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator

class Market(
    private val channel: String,
    private val commands: MarketCommands,
    private val extensionProvider: ExtensionProvider
) {

    private fun onInstallationFinished() {
        extensionProvider.forEach(HelperExtension::class) { helper ->
            helper.registerCommand("!buy")
            helper.registerCommand("!market")
        }
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null
        private var messages: Messages? = null
        private val products: MutableList<Product> = mutableListOf()

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun messages(
            couldNotGetProductPrice: String,
            productNotFound: String,
            usage: String,
            weHaveThisProducts: String,
            youDontHaveEnoughPoints: String,
            yourPurchaseIsPending: String
        ) {
            messages = Messages(
                couldNotGetProductPrice = couldNotGetProductPrice,
                productNotFound = productNotFound,
                usage = usage,
                weHaveThisProducts = weHaveThisProducts,
                youDontHaveEnoughPoints = youDontHaveEnoughPoints,
                yourPurchaseIsPending = yourPurchaseIsPending
            )
        }

        @Dsl
        fun product(product: Product) {
            products.add(product)
        }

        fun build(serviceLocator: ServiceLocator): Market {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Market::class.simpleName}")
            val theMessages = messages
                ?: throw IllegalArgumentException("Messages must be set for the extension ${Market::class.simpleName}")

            val logger = serviceLocator.loggerFactory.getLogger(Market::class)

            val commands = MarketCommands(
                channelName,
                theMessages,
                products,
                logger,
                serviceLocator.extensionProvider,
                serviceLocator
            )
            return Market(
                channelName,
                commands,
                serviceLocator.extensionProvider
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Market> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Market {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { market ->
                    pipeline.interceptCommandEvent(market.commands::interceptCommandEvent)
                    pipeline.requestChannel(market.channel)
                    market.onInstallationFinished()
                }
        }

    }
}