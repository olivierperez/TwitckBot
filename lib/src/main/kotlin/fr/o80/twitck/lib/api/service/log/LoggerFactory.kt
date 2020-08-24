package fr.o80.twitck.lib.api.service.log

import kotlin.reflect.KClass

interface LoggerFactory {
    fun getLogger(klass: KClass<*>) : Logger
}