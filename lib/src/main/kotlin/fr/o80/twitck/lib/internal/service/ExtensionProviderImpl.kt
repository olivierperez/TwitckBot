package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import kotlin.reflect.KClass
import kotlin.reflect.cast

class ExtensionProviderImpl : ExtensionProvider {

    private val extensions = mutableListOf<Any>()

    override fun <T : Any> firstOrNull(extensionInterface: KClass<T>): T? =
        extensions.filterIsInstance(extensionInterface.java).firstOrNull()

    fun register(extension: Any) {
        extensions += extension
    }

}
