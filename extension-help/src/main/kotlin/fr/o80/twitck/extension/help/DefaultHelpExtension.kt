package fr.o80.twitck.extension.help

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.service.ConfigService
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import java.time.Duration

class DefaultHelpExtension(
    private val channel: String,
    private val configuredCommands: Map<String, String>
) : HelpExtension {

    private val commands = mutableMapOf<String, String?>().apply {
        putAll(configuredCommands)
    }

    override fun registerCommand(command: String) {
        commands[command] = null
    }

    private fun interceptCommandEvent(
        messenger: Messenger,
        commandEvent: CommandEvent
    ): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            "!help" -> {
                messenger.sendHelp(commandEvent.channel, commands.keys)
            }
            in commands.keys -> {
                commands[commandEvent.command.tag]?.let { message ->
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
            this.sendImmediately(
                channel,
                "Je ne sais rien faire O_o du moins pour l'instant...",
                coolDown
            )
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

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): HelpExtension? {
            val config = configService.getConfig("help.json", HelpConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            serviceLocator.loggerFactory.getLogger(DefaultHelpExtension::class)
                .info("Installing Help extension...")

            return DefaultHelpExtension(
                config.data.channel.name,
                config.data.commands
            ).also { help ->
                pipeline.requestChannel(config.data.channel.name)
                pipeline.interceptCommandEvent(help::interceptCommandEvent)
            }
        }
    }

}
