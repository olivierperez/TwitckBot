package fr.o80.twitck.extension.ngrok

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class NgrokConfiguration(
    val path: String,
    val token: String,
    val name: String,
    val port: Int
)
