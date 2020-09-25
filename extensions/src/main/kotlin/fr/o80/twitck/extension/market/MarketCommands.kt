package fr.o80.twitck.extension.market

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.Do
import java.time.Duration
import java.util.Date

// TODO OPZ Messages
class MarketCommands(
    private val channel: String,
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
            "!market" -> handleMarketCommand(messenger)
        }

        return commandEvent
    }

    private fun handleBuyCommand(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.isEmpty()) {
            val coolDown = CoolDown(Duration.ofMinutes(1))
            messenger.sendImmediately(channel, "Usage de !buy => !buy <produit> <paramètres>", coolDown)
            return
        }

        val product = products.firstOrNull { product -> product.name == commandEvent.command.options[0] }
        if (product == null) {
            messenger.sendImmediately(channel, "le produit n'existe pas")
            return
        }

        val price = product.computePrice(commandEvent)
        if (price == null) {
            messenger.sendImmediately(
                channel,
                "Impossible de calculer le prix pour l'achat demandé !"
            )
        } else {
            doBuy(messenger, commandEvent, product, price)
        }
    }

    private fun handleMarketCommand(messenger: Messenger) {
        val productNames = products.joinToString(", ") { it.name }
        val coolDown = CoolDown(Duration.ofMinutes(1))
        messenger.sendImmediately(
            channel,
            "Voilà tout ce que j'ai sur l'étagère : #PRODUCTS#".replace("#PRODUCTS#", productNames),
            coolDown
        )
    }

    private fun doBuy(
        messenger: Messenger,
        commandEvent: CommandEvent,
        product: Product,
        price: Int
    ) {
        if (points.consumePoints(commandEvent.login, price)) {
            logger.info("${commandEvent.login} just spend $price points for ${product.name}")

            val purchaseResult = product.execute(messenger, commandEvent, logger, storage, serviceLocator)
            Do exhaustive when (purchaseResult) {
                is PurchaseResult.Success -> onBuySucceeded(messenger, purchaseResult, commandEvent, product)
                is PurchaseResult.Fail -> onBuyFailed(messenger, purchaseResult, commandEvent, product, price)
                is PurchaseResult.WaitingValidation -> onBuyIsWaitingForValidation(
                    messenger,
                    purchaseResult,
                    commandEvent,
                    product
                )
            }
        } else {
            messenger.sendImmediately(
                channel,
                "@${commandEvent.login} tu n'as pas assez de codes source pour cet achat !"
            )
        }
    }

    private fun onBuySucceeded(
        messenger: Messenger,
        purchaseResult: PurchaseResult.Success,
        commandEvent: CommandEvent,
        product: Product
    ) {
        logger.info("${commandEvent.login} just bought a ${product.name}!")
        purchaseResult.message?.let { messenger.sendImmediately(channel, it) }
    }

    private fun onBuyFailed(
        messenger: Messenger,
        purchaseResult: PurchaseResult.Fail,
        commandEvent: CommandEvent,
        product: Product,
        price: Int
    ) {
        logger.info("${commandEvent.login} failed to buy ${product.name}!")
        messenger.sendImmediately(channel, purchaseResult.message)
        points.addPoints(commandEvent.login, price)
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

            logger.info("The purchase ${product.name} is waiting for validation ${commandEvent.login}")
            messenger.sendImmediately(
                channel,
                "${commandEvent.login} ton achat est en attente de validation"
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
