package fr.o80.twitck.extension.market

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.service.step.ActionStep

@JsonClass(generateAdapter = true)
class MarketConfiguration(
    val channel: String,
    val i18n: MarketI18n,
    val products: List<MarketProduct>
)

@JsonClass(generateAdapter = true)
class MarketI18n(
    val productNotFound: String,
    val usage: String,
    val weHaveThisProducts: String,
    val youDontHaveEnoughPoints: String,
    val yourPurchaseIsPending: String
)

@JsonClass(generateAdapter = true)
class MarketProduct(
    val name: String,
    val price: Int,
    val steps: List<ActionStep>
)