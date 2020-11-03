package fr.o80.twitck.lib.api.extension

interface Stat {
    fun hit(namespace: String, key: String, extra: Map<String, Any>)
}