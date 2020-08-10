package fr.o80.twitck.extension.help

import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.bean.Command
import fr.o80.twitck.lib.bean.MessageEvent
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.extension.ExtensionProvider
import fr.o80.twitck.lib.extension.TwitckExtension

class Help(
    private val channel: String,
    private val registeredCommands: MutableMap<String, String?>
) {

    fun interceptMessageEvent(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        println("> I've just seen a message event: ${messageEvent.channel} > ${messageEvent.message}")

        val command = parseCommand(messageEvent)
        reactToCommand(command, bot, messageEvent)

        return messageEvent
    }

    // TODO OPZ Ca c'est du gros C/C
    private fun parseCommand(messageEvent: MessageEvent): Command {
        val split = messageEvent.message.split(" ")
        return if (split.size == 1) {
            Command(messageEvent.badges, split[0])
        } else {
            Command(
                messageEvent.badges,
                split[0],
                split.subList(1, split.size)
            )
        }
    }

    private fun reactToCommand(
        command: Command,
        bot: TwitckBot,
        messageEvent: MessageEvent
    ) {
        when (command.tag) {
            "!help" -> {
                bot.sendHelp(messageEvent.channel, registeredCommands.keys)
            }
            in registeredCommands.keys -> {
                registeredCommands[command.tag]?.let { message ->
                    bot.send(messageEvent.channel, message)
                }
            }
        }
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

    fun registerCommand(command: String) {
        registeredCommands[command] = null
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
            extensionProvider: ExtensionProvider,
            configure: Configuration.() -> Unit
        ): Help {
            return Configuration()
                .apply(configure)
                .build()
                .also { localHelp ->
                    pipeline.interceptMessageEvent(localHelp::interceptMessageEvent)
                }
        }

    }


}
