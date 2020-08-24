package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.ServiceLocator

class Rewards(
    private val channel: String,
    private val commandParser: CommandParser,
    private val extensionProvider: ExtensionProvider
) {

    private val alreadyClaimed: MutableList<String> = mutableListOf()

    private fun interceptMessage(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        commandParser.parse(messageEvent)?.let { command ->
            handleCommand(command)
        }
        return messageEvent
    }

    private fun handleCommand(command: Command) {
        when (command.tag) {
            "!claim" -> claim(command.login)
        }
    }

    private fun claim(login: String) {
        if (alreadyClaimed.any { it == login }) {
            return
        }

        alreadyClaimed.add(login)

        extensionProvider.provide(PointsManager::class.java)
            .filter { it.channel == channel }
            .forEach { pointsManager ->
                pointsManager.addPoints(login, 50)
            }
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        fun build(serviceLocator: ServiceLocator): Rewards {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Rewards::class.simpleName}")

            return Rewards(
                channelName,
                serviceLocator.commandParser,
                serviceLocator.extensionProvider
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Rewards> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Rewards {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { rewards ->
                    pipeline.interceptMessageEvent(rewards::interceptMessage)
                    pipeline.requestChannel(rewards.channel)
                }
        }

    }
}