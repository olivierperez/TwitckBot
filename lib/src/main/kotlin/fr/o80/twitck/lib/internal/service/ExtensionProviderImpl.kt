package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import kotlin.reflect.KClass
import kotlin.reflect.cast

class ExtensionProviderImpl : ExtensionProvider {

    private val extensions = mutableListOf<Any>()

    override fun <T : Any> first(extensionInterface: KClass<T>): T =
        extensions.first { extensionInterface.isInstance(it) }
            .let { extension -> extensionInterface.cast(extension) }

    override fun <T : Any> provide(extensionInterface: KClass<T>): List<T> =
        extensions
            .filter { extension -> extensionInterface.isInstance(extension) }
            .map { extension -> extensionInterface.cast(extension) }

    override fun <T : Any> forEach(extensionInterface: KClass<T>, block: (extension: T) -> Unit) {
        provide(extensionInterface)
            .forEach(block)
    }

    fun register(extension: Any) {
        extensions += extension
    }

}
