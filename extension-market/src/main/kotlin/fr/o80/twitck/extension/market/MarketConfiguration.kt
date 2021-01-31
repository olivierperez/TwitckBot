package fr.o80.twitck.extension.market

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class MarketConfiguration(
    val channel: String,
    val messages: MarketMessages,
    val products: List<MarketProduct>
)

@JsonClass(generateAdapter = true)
class MarketMessages(
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
    val type: ProductType,
    val action: String
)

enum class ProductType {
    COMMAND, MESSAGE
}
