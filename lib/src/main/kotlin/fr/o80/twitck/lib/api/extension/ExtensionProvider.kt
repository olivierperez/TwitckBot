package fr.o80.twitck.lib.api.extension

import kotlin.reflect.KClass

interface ExtensionProvider {
    fun <T : Any> provide(extensionInterface: KClass<T>): List<T>
    // TODO OPZ fun <T : Any> forEach(extensionInterface: KClass<T>, block: (extension: T) -> Unit): List<T>
}