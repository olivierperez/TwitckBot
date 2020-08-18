package fr.o80.twitck.lib.api.extension

interface ExtensionProvider {
    fun <T> provide(extensionInterface: Class<T>): List<T>
}