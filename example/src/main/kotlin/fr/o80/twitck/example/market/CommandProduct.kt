package fr.o80.twitck.example.market

import fr.o80.twitck.extension.market.Product
import fr.o80.twitck.extension.market.PurchaseResult
import fr.o80.twitck.extension.runtimecommand.RuntimeCommand
import fr.o80.twitck.extension.runtimecommand.SCOPE_PERMANENT
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.skip


object CommandProduct : Product {

    override val name: String = "command"

    override fun computePrice(commandEvent: CommandEvent): Int = 200

    override fun execute(
            messenger: Messenger,
            commandEvent: CommandEvent,
            logger: Logger,
            storageExtension: StorageExtension,
            serviceLocator: ServiceLocator
    ): PurchaseResult {
        val msg = commandEvent.command.options.skip(1).joinToString(" ")
        val commandTag = "!${commandEvent.viewer.login}"

        serviceLocator.extensionProvider.forEach(RuntimeCommand::class) { runtimeCommand ->
            runtimeCommand.registerRuntimeCommand(commandTag, SCOPE_PERMANENT, msg)
        }

        return PurchaseResult.Success("Yo ${commandEvent.viewer.displayName}, tu viens d'acheter la commande $commandTag")
    }

}
