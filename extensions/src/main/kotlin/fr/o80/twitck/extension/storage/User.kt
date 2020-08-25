package fr.o80.twitck.extension.storage

data class User(
    val login: String
) {
    private val extras: MutableMap<String, String> = mutableMapOf()

    fun putExtra(key: String, value: String) {
        extras[key] = value
    }

    fun getExtra(key: String): String? {
        return extras[key]
    }
}