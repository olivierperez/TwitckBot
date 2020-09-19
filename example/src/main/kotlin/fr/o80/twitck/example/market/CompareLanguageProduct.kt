package fr.o80.twitck.example.market

import fr.o80.twitck.extension.market.Product
import fr.o80.twitck.extension.market.PurchaseResult
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.Deadline
import fr.o80.twitck.lib.api.bean.SendMessage
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger

object CompareLanguageProduct : Product {

    override val name: String = "language"

    override fun computePrice(commandEvent: CommandEvent): Int = 20

    override fun execute(
        messenger: Messenger,
        commandEvent: CommandEvent,
        logger: Logger,
        storageExtension: StorageExtension,
        serviceLocator: ServiceLocator
    ): PurchaseResult {
        if (commandEvent.command.options.size != 3)
            return PurchaseResult.Fail("Usage: !buy $name <langage1> <langage2>")

        val (better, worse) = extractLanguages(commandEvent)

        return if (worse.equals("kotlin", true)) {
            PurchaseResult.Fail("Rien n'est mieux que Kotlin, achat non pris en compte")
        } else {
            messenger.send(
                SendMessage(commandEvent.channel, "Tout le monde préfère $better à $worse", Deadline.Immediate)
            )
            PurchaseResult.Success()
        }
    }

    private fun extractLanguages(commandEvent: CommandEvent): Pair<String, String> {
        val options = commandEvent.command.options
        return Pair(options[1], options[2])
    }

}