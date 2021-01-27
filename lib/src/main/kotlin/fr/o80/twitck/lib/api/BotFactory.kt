package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.internal.service.ConfigService
import fr.o80.twitck.lib.internal.service.ConfigServiceImpl
import java.io.File

class BotFactory(
    private val configDirectory: File,
    oauthToken: String,
    hostName: String,
) {

    private val configService: ConfigService = ConfigServiceImpl()

    init {
        check(configDirectory.isDirectory) { "The config path is NOT a directory!" }
    }

    fun create(): TwitckBot {
        configService.getConfigsFrom(configDirectory)
        TODO()
    }
}
