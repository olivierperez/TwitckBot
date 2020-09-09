package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger

class Product(
    val name: String,
    val price: Int,
    val executePurchase: PurchaseExecution
)

typealias PurchaseExecution = (CommandEvent, Logger, StorageExtension, ServiceLocator) -> PurchaseResult

sealed class PurchaseResult {
    class Success(val message: String) : PurchaseResult()
    class Fail(val message: String) : PurchaseResult()
    object WaitingValidation : PurchaseResult()
}
