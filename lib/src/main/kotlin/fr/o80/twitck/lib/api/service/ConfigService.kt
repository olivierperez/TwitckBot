package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.internal.bean.ExtensionConfig
import kotlin.reflect.KClass

interface ConfigService {
    fun <T : Any> getConfig(file: String, clazz: KClass<T>): ExtensionConfig<T>?
}
