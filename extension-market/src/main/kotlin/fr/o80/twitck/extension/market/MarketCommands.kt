package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import java.time.Duration

class MarketCommands(
    private val channel: String,
    private val messages: MarketMessages,
    private val products: List<MarketProduct>,
    private val logger: Logger,
    private val extensionProvider: ExtensionProvider
) {

    private val points: PointsExtension by lazy {
        extensionProvider.provide(PointsExtension::class).first()
    }

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!buy" -> handleBuyCommand(messenger, commandEvent)
            "!market" -> handleMarketCommand(messenger, commandEvent)
        }

        return commandEvent
    }

    private fun handleBuyCommand(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.isEmpty()) {
            extensionProvider.first(OverlayExtension::class)
                .alert(messages.usage, Duration.ofSeconds(10))
            return
        }

        val product =
            products.firstOrNull { product -> product.name == commandEvent.command.options[0] }
        if (product == null) {
            extensionProvider.first(OverlayExtension::class)
                .alert(messages.productNotFound, Duration.ofSeconds(10))
            return
        }

        val price = product.price
        doBuy(messenger, commandEvent, product, price)
    }

    private fun handleMarketCommand(messenger: Messenger, commandEvent: CommandEvent) {
        val productNames = products.joinToString(", ") { it.name }
        val message = messages.weHaveThisProducts.replace("#PRODUCTS#", productNames)
        messenger.sendImmediately(commandEvent.channel, message, CoolDown.ofSeconds(15))
    }

    private fun doBuy(
        messenger: Messenger,
        commandEvent: CommandEvent,
        product: MarketProduct,
        price: Int
    ) {
        messenger.sendImmediately(channel, "Market is closed")
        // TODO OPZ
        return
        if (points.consumePoints(commandEvent.viewer.login, price)) {
            logger.info("${commandEvent.viewer.displayName} just spend $price points for ${product.name}")

            /*val purchaseResult = product.execute(messenger, commandEvent, logger, storage, serviceLocator)
            Do exhaustive when (purchaseResult) {
                is PurchaseResult.Success ->
                    onBuySucceeded(purchaseResult, commandEvent, product)
                is PurchaseResult.Fail ->
                    onBuyFailed(purchaseResult, commandEvent, product, price)
                is PurchaseResult.WaitingValidation ->
                    onBuyIsWaitingForValidation(
                        messenger,
                        purchaseResult,
                        commandEvent,
                        product
                    )
            }*/
        } else {
            messenger.sendImmediately(
                channel,
                messages.youDontHaveEnoughPoints.replace("#USER#", commandEvent.viewer.displayName)
            )
        }
    }

}
