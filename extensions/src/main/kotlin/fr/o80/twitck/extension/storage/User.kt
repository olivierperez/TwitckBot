package fr.o80.twitck.extension.storage

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val login: String,
    val extras: MutableMap<String, String> = mutableMapOf()
) {

    fun putExtra(key: String, value: String) {
        extras[key] = value
    }

    fun getExtra(key: String): String? {
        return extras[key]
    }
}