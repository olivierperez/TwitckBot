package fr.o80.twitck.lib.api.extension

import kotlin.reflect.KClass

interface ExtensionProvider {
    fun <T : Any> firstOrNull(extensionInterface: KClass<T>): T?
}
