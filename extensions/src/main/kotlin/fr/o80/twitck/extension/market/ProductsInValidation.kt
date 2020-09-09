package fr.o80.twitck.extension.market

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal class ProductsInValidation(
    val products: MutableList<ProductInValidation> = mutableListOf()
)

@JsonClass(generateAdapter = true)
internal class ProductInValidation(
    val login: String,
    val code: String,
    val message: String,
    val price: Int,
    val date: Date
)