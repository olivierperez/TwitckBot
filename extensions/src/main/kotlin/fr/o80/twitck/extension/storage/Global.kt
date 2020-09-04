package fr.o80.twitck.extension.storage

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Global(
    val namespaces: MutableMap<String, Extras> = mutableMapOf()
) {

    fun putExtra(namespace: String, key: String, value: String) {
        namespaces.compute(namespace) { _, extras ->
            (extras ?: Extras()).apply {
                put(key, value)
            }
        }
    }

    fun getExtras(namespace: String): List<Pair<String, String>> {
        return namespaces[namespace]?.getAll() ?: emptyList()
    }
}

@JsonClass(generateAdapter = true)
class Extras(
    val extras: MutableMap<String, String> = mutableMapOf()
) {
    fun put(key: String, value: String) {
        extras[key] = value
    }

    fun getAll(): List<Pair<String, String>> {
        return extras.entries.map { it.toPair() }
    }
}