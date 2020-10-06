package fr.o80.twitck.lib.api.service.log

import fr.o80.twitck.lib.api.bean.Command

interface Logger {
    fun command(command: Command, message: String)
    fun trace(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}