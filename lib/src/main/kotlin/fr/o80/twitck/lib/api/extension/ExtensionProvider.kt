package fr.o80.twitck.lib.api.extension

import kotlin.reflect.KClass

interface ExtensionProvider {
    fun <T : Any> firstOrNull(extensionInterface: KClass<T>): T?
    fun <T : Any> provide(extensionInterface: KClass<T>): List<T>
    fun <T : Any> forEach(extensionInterface: KClass<T>, block: (extension: T) -> Unit)
}