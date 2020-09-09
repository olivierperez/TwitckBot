package fr.o80.twitck.example

import fr.o80.twitck.extension.RuntimeCommand
import fr.o80.twitck.extension.SCOPE_PERMANENT
import fr.o80.twitck.extension.market.PurchaseResult
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.skip


fun marketBuyCommand(
    commandEvent: CommandEvent,
    logger: Logger,
    storageExtension: StorageExtension,
    serviceLocator: ServiceLocator
): PurchaseResult {
    val msg = commandEvent.command.options.skip(1).joinToString(" ")
    val commandTag = "!${commandEvent.login}"
    serviceLocator.extensionProvider.forEach(RuntimeCommand::class) { runtimeCommand ->
        runtimeCommand.registerRuntimeCommand(commandTag, SCOPE_PERMANENT, msg)
    }
    return PurchaseResult.Success("Yo ${commandEvent.login}, tu viens d'acheter la commande $commandTag")
}

fun marketBuyCodeReview(
    commandEvent: CommandEvent,
    logger: Logger,
    storageExtension: StorageExtension,
    serviceLocator: ServiceLocator
): PurchaseResult {
    // TODO
    return PurchaseResult.Fail("Not yet implemented")
}
