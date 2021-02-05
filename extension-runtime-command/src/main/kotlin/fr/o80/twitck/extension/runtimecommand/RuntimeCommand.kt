package fr.o80.twitck.extension.runtimecommand

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.service.ConfigService
import java.time.Duration
import java.util.*

const val SCOPE_STREAM = "stream"
const val SCOPE_PERMANENT = "permanent"

// TODO OPZ Extraire la partie gestion des commandes
class RuntimeCommand(
    private val channel: String,
    private val privilegedBadges: Collection<Badge>,
    private val storage: StorageExtension?,
    private val help: HelpExtension?,
    private val logger: Logger
) {

    private val namespace: String = RuntimeCommand::class.java.name

    private val runtimeCommands = mutableMapOf<String, String?>()

    init {
        storage?.run {
            getGlobalInfo(namespace)
                .filter { it.first.startsWith("Command//") }
                .forEach {
                    val commandTag = it.first.substring("Command//".length)
                    runtimeCommands[commandTag] = it.second
                    help?.registerCommand(commandTag)
                }
        }
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
        if (commandEvent.viewer hasNoPrivilegesOf privilegedBadges) return

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
            val coolDown = CoolDown(Duration.ofSeconds(5))
            messenger.sendImmediately(commandEvent.channel, message, coolDown)
        }
    }

    private fun registerRuntimeCommand(newCommand: String, scope: String, message: String) {
        runtimeCommands[newCommand] = message

        if (scope == SCOPE_PERMANENT) {
            if (storage!= null) {
                storage.putGlobalInfo(namespace, "Command//$newCommand", message)
            } else {
                logger.error("You're trying to store a Permanent command but you haven't configured a Storage extension")
            }
        }
    }

    private fun registerToHelper(newCommand: String) {
        help?.registerCommand(newCommand)
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): RuntimeCommand? {
            val config = configService.getConfig("runtime_commands.json", RuntimeCommandConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val storage = serviceLocator.extensionProvider.firstOrNull(StorageExtension::class)
            val help = serviceLocator.extensionProvider.firstOrNull(HelpExtension::class)
            val logger = serviceLocator.loggerFactory.getLogger(RuntimeCommand::class)

            return RuntimeCommand(
                config.data.channel,
                config.data.privilegedBadges,
                storage,
                help,
                logger
            ).also { runtimeCommand ->
                pipeline.requestChannel(config.data.channel)
                pipeline.interceptCommandEvent { messenger, commandEvent ->
                    runtimeCommand.interceptCommandEvent(
                        messenger,
                        commandEvent
                    )
                }
            }
        }
    }

}
