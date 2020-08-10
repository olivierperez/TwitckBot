package fr.o80.twitck.extension

import fr.o80.twitck.extension.help.Help
import fr.o80.twitck.lib.ExtensionProvider
import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.bean.Badge
import fr.o80.twitck.lib.bean.Command
import fr.o80.twitck.lib.bean.MessageEvent
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.extension.TwitckExtension

class RuntimeCommand(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val extensionProvider: ExtensionProvider
) {

    private val runtimeCommands = mutableMapOf<String, String?>()

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
        val newCommand = options[0].let { cmd -> if(cmd[0] == '!') cmd else "!$cmd" }
        val message = options.subList(1, options.size).joinToString(" ")
        runtimeCommands[newCommand] = message
        extensionProvider.provide(Help::class.java)
            ?.registerCommand(newCommand)
        return newCommand
    }

    class Configuration(private val extensionProvider: ExtensionProvider) {

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

        fun build(): RuntimeCommand {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${RuntimeCommand::class.simpleName}")
            val theBadges = badges ?: arrayOf(Badge.BROADCASTER)
            return RuntimeCommand(channelName, theBadges, extensionProvider)
        }
    }

    companion object Extension : TwitckExtension<Configuration, RuntimeCommand> {
        override fun install(pipeline: Pipeline, extensionProvider: ExtensionProvider, configure: Configuration.() -> Unit): RuntimeCommand {
            return Configuration(extensionProvider)
                .apply(configure)
                .build()
                .also { runtimeCommand ->
                    pipeline.requestChannel(runtimeCommand.channel)
                    pipeline.interceptMessageEvent(runtimeCommand::interceptMessageEvent)
                }
        }
    }
}
