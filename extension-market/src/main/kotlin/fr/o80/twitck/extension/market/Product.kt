package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import java.util.Date

interface Product {
    val name: String

    fun computePrice(
        commandEvent: CommandEvent
    ): Int?

    fun execute(
            messenger: Messenger,
            commandEvent: CommandEvent,
            logger: Logger,
            storageExtension: StorageExtension,
            serviceLocator: ServiceLocator
    ): PurchaseResult
}

sealed class PurchaseResult {
    class Success(val message: String? = null) : PurchaseResult()
    class Fail(val message: String) : PurchaseResult()
    class WaitingValidation(
        val login: String,
        val code: String,
        val message: String,
        val price: Int,
        val date: Date = Date()
    ) : PurchaseResult()
}
