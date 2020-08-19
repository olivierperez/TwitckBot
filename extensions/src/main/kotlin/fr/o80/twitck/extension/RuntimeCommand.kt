package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.ServiceLocator
import java.util.Locale

class RuntimeCommand(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val extensionProvider: ExtensionProvider,
    private val commandParser: CommandParser
) {

    private val runtimeCommands = mutableMapOf<String, String?>()

    fun interceptMessageEvent(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        val command = commandParser.parse(messageEvent)
        reactToCommand(command, bot, messageEvent)

        return messageEvent
    }

    private fun reactToCommand(
        command: Command,
        bot: TwitckBot,
        messageEvent: MessageEvent
    ) {
        when (command.tag) {
            "!addcmd" -> {
                if (command.badges.any { it in privilegedBadges }) {
                    val addedCommand = addCommand(command.options)
                    bot.send(messageEvent.channel, "Commande $addedCommand ajoutée")
                }
            }
            in runtimeCommands.keys -> {
                runtimeCommands[command.tag]?.let { message ->
                    bot.send(messageEvent.channel, message)
                }
            }
        }
    }

    private fun addCommand(options: List<String>): String {
        val newCommand = options[0].addPrefix().toLowerCase(Locale.FRENCH)
        val message = options.subList(1, options.size).joinToString(" ")
        registerRuntimeCommand(newCommand, message)
        registerToHelper(newCommand)
        return newCommand
    }

    private fun registerRuntimeCommand(newCommand: String, message: String) {
        runtimeCommands[newCommand] = message
    }

    private fun registerToHelper(newCommand: String) {
        extensionProvider.provide(HelperExtension::class.java).forEach { helper -> helper.registerCommand(newCommand) }
    }

    class Configuration {

        @DslMarker
        private annotation class RuntimeCommandDsl

        private var channel: String? = null
        private var badges: Array<out Badge>? = null

        @RuntimeCommandDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @RuntimeCommandDsl
        fun privilegedBadges(vararg badges: Badge) {
            if (badges.isEmpty()) {
                throw IllegalArgumentException("Impossible to set an empty list of privileged badges.")
            }
            this.badges = badges
        }

        fun build(serviceLocator: ServiceLocator): RuntimeCommand {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${RuntimeCommand::class.simpleName}")
            val theBadges = badges ?: arrayOf(Badge.BROADCASTER)
            return RuntimeCommand(
                channelName,
                theBadges,
                serviceLocator.extensionProvider,
                serviceLocator.commandParser
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, RuntimeCommand> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): RuntimeCommand {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { runtimeCommand ->
                    pipeline.requestChannel(runtimeCommand.channel)
                    pipeline.interceptMessageEvent(runtimeCommand::interceptMessageEvent)
                }
        }
    }
}

private fun String.addPrefix(): String {
    return if (this[0] == '!') this else "!$this"
}
