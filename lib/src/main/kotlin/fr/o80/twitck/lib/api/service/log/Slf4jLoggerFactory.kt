package fr.o80.twitck.lib.api.service.log

import fr.o80.twitck.lib.api.bean.Command
import kotlin.reflect.KClass

class Slf4jLoggerFactory : LoggerFactory {
    override fun getLogger(klass: KClass<*>): Logger =
        Slf4jLoggerAdapter(klass)

    override fun getLogger(name: String): Logger =
        Slf4jLoggerAdapter(name)
}

private class Slf4jLoggerAdapter(name: String) : Logger {

    constructor(klass: KClass<*>) : this(klass.java.name)

    private val slf4jLogger: org.slf4j.Logger =
        org.slf4j.LoggerFactory.getLogger(name)

    override fun command(command: Command, message: String) {
        slf4jLogger.debug("[Command:${command.tag}] $message")
    }

    override fun info(message: String) {
        slf4jLogger.info(message)
    }

    override fun debug(message: String) {
        slf4jLogger.debug(message)
    }
}