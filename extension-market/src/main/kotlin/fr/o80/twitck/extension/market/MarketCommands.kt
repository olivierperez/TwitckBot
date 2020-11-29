package fr.o80.twitck.extension.market

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.*
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.Do
import java.time.Duration
import java.util.*

class MarketCommands(
    private val channel: String,
    private val messages: Messages,
    private val products: MutableList<Product>,
    private val logger: Logger,
    private val extensionProvider: ExtensionProvider,
    private val serviceLocator: ServiceLocator
) {

    private val moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = Market::class.java.name

    private val points: PointsManager by lazy {
        extensionProvider.provide(PointsManager::class).first()
    }

    private val lockWaitingForValidation = Any()

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!buy" -> handleBuyCommand(messenger, commandEvent)
            "!market" -> handleMarketCommand()
        }

        return commandEvent
    }

    private fun handleBuyCommand(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.isEmpty()) {
            extensionProvider.first(OverlayExtension::class)
                .alert(messages.usage, Duration.ofSeconds(10))
            return
        }

        val product = products.firstOrNull { product -> product.name == commandEvent.command.options[0] }
        if (product == null) {
            extensionProvider.first(OverlayExtension::class)
                .alert(messages.productNotFound, Duration.ofSeconds(10))
            return
        }

        val price = product.computePrice(commandEvent)
        if (price == null) {
            extensionProvider.first(OverlayExtension::class)
                .alert(messages.couldNotGetProductPrice, Duration.ofSeconds(10))
        } else {
            doBuy(messenger, commandEvent, product, price)
        }
    }

    private fun handleMarketCommand() {
        val productNames = products.joinToString(", ") { it.name }
        val message = messages.weHaveThisProducts.replace("#PRODUCTS#", productNames)
        extensionProvider.first(OverlayExtension::class)
            .alert(message, Duration.ofSeconds(15))
    }

    private fun doBuy(
        messenger: Messenger,
        commandEvent: CommandEvent,
        product: Product,
        price: Int
    ) {
        if (points.consumePoints(commandEvent.viewer.login, price)) {
            logger.info("${commandEvent.viewer.displayName} just spend $price points for ${product.name}")

            val purchaseResult = product.execute(messenger, commandEvent, logger, storage, serviceLocator)
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
            }
        } else {
            messenger.sendImmediately(
                channel,
                messages.youDontHaveEnoughPoints.replace("#USER#", commandEvent.viewer.displayName)
            )
        }
    }

    private fun onBuySucceeded(
        purchaseResult: PurchaseResult.Success,
        commandEvent: CommandEvent,
        product: Product
    ) {
        logger.info("${commandEvent.viewer.displayName} just bought a ${product.name}!")
        purchaseResult.message?.let { message ->
            extensionProvider.first(OverlayExtension::class)
                .alert(message, Duration.ofSeconds(15))
        }
        extensionProvider.forEach(SoundExtension::class) { sound -> sound.playYoupi() }
    }

    private fun onBuyFailed(
        purchaseResult: PurchaseResult.Fail,
        commandEvent: CommandEvent,
        product: Product,
        price: Int
    ) {
        logger.info("${commandEvent.viewer.displayName} failed to buy ${product.name}!")
        extensionProvider.first(OverlayExtension::class)
            .alert(purchaseResult.message, Duration.ofSeconds(10))
        points.addPoints(commandEvent.viewer.login, price)
    }

    private fun onBuyIsWaitingForValidation(
            messenger: Messenger,
            purchaseResult: PurchaseResult.WaitingValidation,
            commandEvent: CommandEvent,
            product: Product
    ) {
        synchronized(lockWaitingForValidation) {
            val productsInValidation = storage.getOrCreateProductsInValidation()

            productsInValidation.products += ProductInValidation(
                login = purchaseResult.login,
                code = purchaseResult.code,
                message = purchaseResult.message,
                price = purchaseResult.price,
                date = purchaseResult.date
            )

            storage.putProductsInValidation(productsInValidation)

            logger.info("The purchase ${product.name} is waiting for validation ${commandEvent.viewer.displayName}")
            messenger.sendImmediately(
                channel,
                messages.yourPurchaseIsPending.replace("#USER#", commandEvent.viewer.displayName)
            )
        }
    }

    private fun StorageExtension.getOrCreateProductsInValidation(): ProductsInValidation {
        return this.getGlobalInfo(namespace)
            .firstOrNull { it.first == "waitingForValidation" }
            ?.let { moshi.adapter(ProductsInValidation::class.java).fromJson(it.second) }
            ?: ProductsInValidation()
    }

    private fun StorageExtension.putProductsInValidation(productsInValidation: ProductsInValidation) {
        val json = moshi.adapter(ProductsInValidation::class.java).toJson(productsInValidation)
        this.putGlobalInfo(namespace, "waitingForValidation", json)
    }

}
