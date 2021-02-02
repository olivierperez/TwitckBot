package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.api.service.step.StepParam
import fr.o80.twitck.lib.api.service.step.StepsExecutor

class MarketCommands(
    private val channel: String,
    private val i18n: MarketI18n,
    private val products: List<MarketProduct>,
    private val logger: Logger,
    private val extensionProvider: ExtensionProvider,
    private val stepsExecutor: StepsExecutor
) {

    private val pointsExtension: PointsExtension by lazy {
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
            messenger.sendImmediately(
                commandEvent.channel,
                i18n.usage,
                CoolDown.ofSeconds(10)
            )
            return
        }

        val product = products.firstOrNull { product ->
            product.name == commandEvent.command.options[0]
        }

        if (product == null) {
            messenger.sendImmediately(
                commandEvent.channel,
                i18n.productNotFound,
                CoolDown.ofSeconds(10)
            )
            return
        }

        doBuy(messenger, commandEvent, product, product.price)
    }

    private fun handleMarketCommand(messenger: Messenger, commandEvent: CommandEvent) {
        val productNames = products.joinToString(", ") { it.name }
        val message = i18n.weHaveThisProducts.replace("#PRODUCTS#", productNames)
        messenger.sendImmediately(commandEvent.channel, message, CoolDown.ofSeconds(15))
    }

    private fun doBuy(
        messenger: Messenger,
        commandEvent: CommandEvent,
        product: MarketProduct,
        price: Int
    ) {
        if (pointsExtension.consumePoints(commandEvent.viewer.login, price)) {
            try {
                logger.info("${commandEvent.viewer.displayName} just spend $price points for ${product.name}")

                val param = StepParam.fromCommand(commandEvent, skipOptions = 1)
                stepsExecutor.execute(product.steps, messenger, param)
            } catch (e: Exception) {
                logger.error("Failed to execute purchase!", e)
                pointsExtension.addPoints(commandEvent.viewer.login, price)
            }
        } else {
            messenger.sendImmediately(
                channel,
                i18n.youDontHaveEnoughPoints.replace("#USER#", commandEvent.viewer.displayName)
            )
        }
    }

}
