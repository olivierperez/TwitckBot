package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import java.time.Duration
import java.util.Locale

const val SCOPE_STREAM = "stream"
const val SCOPE_PERMANENT = "permanent"

// TODO OPZ Extraire la partie gestion des commandes
class RuntimeCommand(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val extensionProvider: ExtensionProvider,
    private val logger: Logger
) {

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = RuntimeCommand::class.java.name

    private val runtimeCommands = mutableMapOf<String, String?>()

    private fun onInstallationFinished() {
        storage.getGlobalInfo(namespace)
            .filter { it.first.startsWith("Command//") }
            .forEach { runtimeCommands[it.first.substring("Command//".length)] = it.second }
    }

    private fun interceptCommandEvent(
        messenger: Messenger,
        commandEvent: CommandEvent
    ): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        // !cmd stream !exo Aujourd'hui on développe des trucs funs
        // !cmd permanent !twitter Retrouvez-moi sur https://twitter.com/olivierperez
        when (commandEvent.command.tag) {
            "!cmd" -> handleAddCommand(messenger, commandEvent)
            in runtimeCommands.keys -> handleRegisteredCommand(messenger, commandEvent)
        }

        return commandEvent
    }

    private fun handleAddCommand(
        messenger: Messenger,
        commandEvent: CommandEvent
    ) {
        if (privilegedBadges.intersect(commandEvent.badges).isEmpty()) {
            return
        }

        val command = commandEvent.command
        val scope: String = command.options[0]
        val newCommand: String = command.options[1].toLowerCase(Locale.FRENCH)
        val message: String = command.options.subList(2, command.options.size).joinToString(" ")

        if (scope !in arrayOf(SCOPE_STREAM, SCOPE_PERMANENT)) {
            logger.error("Scope \"$scope\" non autorisé")
            return
        }
        if (!newCommand.startsWith("!")) {
            logger.error("Préfixe manquant pour $newCommand")
            return
        }

        registerRuntimeCommand(newCommand, scope, message)
        registerToHelper(newCommand)
        messenger.sendImmediately(commandEvent.channel, "Commande $newCommand ajoutée")
    }

    private fun handleRegisteredCommand(
        messenger: Messenger,
        commandEvent: CommandEvent
    ) {
        runtimeCommands[commandEvent.command.tag]?.let { message ->
            val coolDown = CoolDown(Duration.ofMinutes(1))
            messenger.sendImmediately(commandEvent.channel, message, coolDown)
        }
    }

    fun registerRuntimeCommand(newCommand: String, scope: String, message: String) {
        runtimeCommands[newCommand] = message

        if (scope == SCOPE_PERMANENT) {
            storage.putGlobalInfo(namespace, "Command//$newCommand", message)
        }
    }

    private fun registerToHelper(newCommand: String) {
        extensionProvider.forEach(HelperExtension::class) { helper -> helper.registerCommand(newCommand) }
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
                serviceLocator.loggerFactory.getLogger(RuntimeCommand::class)
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
                    pipeline.interceptCommandEvent(runtimeCommand::interceptCommandEvent)
                    runtimeCommand.onInstallationFinished()
                }
        }
    }
}
