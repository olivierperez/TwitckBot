package fr.o80.twitck.extension.help

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import java.time.Duration

class Help(
    private val channel: String,
    private val registeredCommands: MutableMap<String, String?>
) : HelperExtension {

    override fun registerCommand(command: String) {
        registeredCommands[command] = null
    }

    private fun interceptCommandEvent(
        messenger: Messenger,
        commandEvent: CommandEvent
    ): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            "!help" -> {
                messenger.sendHelp(commandEvent.channel, registeredCommands.keys)
            }
            in registeredCommands.keys -> {
                registeredCommands[commandEvent.command.tag]?.let { message ->
                    val coolDown = CoolDown(Duration.ofMinutes(1))
                    messenger.sendImmediately(commandEvent.channel, message, coolDown)
                }
            }
        }

        return commandEvent
    }

    private fun Messenger.sendHelp(
        channel: String,
        commands: Collection<String>
    ) {
        if (commands.isEmpty()) {
            val coolDown = CoolDown(Duration.ofMinutes(1))
            this.sendImmediately(channel, "Je ne sais rien faire O_o du moins pour l'instant...", coolDown)
        } else {
            val coolDown = CoolDown(Duration.ofMinutes(1))
            val commandsExamples = commands.joinToString(", ")
            this.sendImmediately(
                channel,
                "Je sais faire un paquet de choses, par exemple : $commandsExamples",
                coolDown
            )
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

        fun build(): Help {
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
                .build()
                .also { localHelp ->
                    pipeline.interceptCommandEvent(localHelp::interceptCommandEvent)
                }
        }
    }


}
