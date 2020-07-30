package fr.o80.twitck.example.extension.help

import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.bean.Badge
import fr.o80.twitck.lib.bean.MessageEvent
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.extension.TwitckExtension

class Help(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    registeredCommands: MutableMap<String, String?>
) {

    private val runtimeCommands = mutableMapOf<String, String?>()

    init {
        registeredCommands.forEach { (k, v) -> runtimeCommands[k] = v }
    }

    fun interceptMessageEvent(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        println("> I've just seen a message event: ${messageEvent.channel} > ${messageEvent.message}")

        val command = parseCommand(messageEvent)
        reactToCommand(command, bot, messageEvent)

        return messageEvent
    }

    private fun parseCommand(messageEvent: MessageEvent): Command {

        val split = messageEvent.message.split(" ")
        return if (split.size == 1) {
            Command(messageEvent.badges, split[0])
        } else {
            Command(messageEvent.badges, split[0], split.subList(1, split.size))
        }
    }

    private fun reactToCommand(
        command: Command,
        bot: TwitckBot,
        messageEvent: MessageEvent
    ) {
        when (command.tag) {
            "!help" -> {
                bot.sendHelp(messageEvent.channel, runtimeCommands.keys)
            }
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

    private fun addCommand(options: List<String>): String {
        val newCommand = options[0]
        val message = options.subList(1, options.size).joinToString(" ")
        runtimeCommands[newCommand] = message
        return newCommand
    }

    class Configuration {

        @DslMarker
        annotation class HelpDsl

        private var channel: String? = null
        private var badges: Array<out Badge>? = null
        private var registeredCommands = mutableMapOf<String, String?>()

        @HelpDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @HelpDsl
        fun privilegedBadges(vararg badges: Badge) {
            if (badges.isEmpty()) {
                throw IllegalArgumentException("Impossible to set an empty list of privileged badges.")
            }
            this.badges = badges
        }

        @HelpDsl
        fun registerCommand(command: String, message: String? = null) {
            registeredCommands[command] = message
        }

        fun build(): Help {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Help::class.simpleName}")
            val theBadges = badges ?: arrayOf(Badge.BROADCASTER)

            return Help(channelName, theBadges, registeredCommands)
        }
    }

    companion object Extension : TwitckExtension<Configuration, Help> {
        override fun install(pipeline: Pipeline, configure: Configuration.() -> Unit): Help {
            return Configuration()
                .apply(configure)
                .build()
                .also { localHelp ->
                    pipeline.interceptMessageEvent(localHelp::interceptMessageEvent)
                }
        }

    }
}

private class Command(
    val badges: List<Badge>,
    val tag: String,
    val options: List<String> = emptyList()
)
