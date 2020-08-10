package fr.o80.twitck.lib.extension

interface ExtensionProvider {
    fun <T> provide(extensionInterface: Class<T>): List<T>
}