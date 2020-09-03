package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator

class Help(
    private val channel: String,
    private val registeredCommands: MutableMap<String, String?>
) : HelperExtension {

    override fun registerCommand(command: String) {
        registeredCommands[command] = null
    }

    private fun interceptCommandEvent(
        bot: TwitckBot,
        commandEvent: CommandEvent
    ): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            "!help" -> {
                bot.sendHelp(commandEvent.channel, registeredCommands.keys)
            }
            in registeredCommands.keys -> {
                registeredCommands[commandEvent.command.tag]?.let { message ->
                    bot.send(commandEvent.channel, message)
                }
            }
        }

        return commandEvent
    }

    private fun TwitckBot.sendHelp(
        channel: String,
        commands: Collection<String>
    ) {
        if (commands.isEmpty()) {
            this.send(channel, "Je ne sais rien faire O_o du moins pour l'instant...")
        } else {
            val commandsExamples = commands.joinToString(", ")
            this.send(channel, "Je sais faire un paquet de choses, par exemple : $commandsExamples")
        }
    }

    class Configuration {

        @DslMarker
        private annotation class HelpDsl

        private var channel: String? = null
        private var registeredCommands = mutableMapOf<String, String?>()

        @HelpDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @HelpDsl
        fun registerCommand(command: String, message: String? = null) {
            registeredCommands[command] = message
        }

        fun build(serviceLocator: ServiceLocator): Help {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Help::class.simpleName}")

            return Help(channelName, registeredCommands)
        }
    }

    companion object Extension : TwitckExtension<Configuration, Help> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Help {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { localHelp ->
                    pipeline.interceptCommandEvent(localHelp::interceptCommandEvent)
                }
        }
    }


}
