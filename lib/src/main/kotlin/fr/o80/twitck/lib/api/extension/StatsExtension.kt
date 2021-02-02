package fr.o80.twitck.lib.api.extension

interface StatsExtension {
    fun hit(namespace: String, key: String, extra: Map<String, Any>)
}