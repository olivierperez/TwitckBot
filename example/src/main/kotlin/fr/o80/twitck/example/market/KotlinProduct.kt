package fr.o80.twitck.example.market

import fr.o80.twitck.extension.market.Product
import fr.o80.twitck.extension.market.PurchaseResult
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.skip

object KotlinProduct : Product {
    override val name: String
        get() = "kotlin"

    override fun computePrice(commandEvent: CommandEvent): Int = 10

    override fun execute(
        bot: TwitckBot,
        commandEvent: CommandEvent,
        logger: Logger,
        storageExtension: StorageExtension,
        serviceLocator: ServiceLocator
    ): PurchaseResult {
        if (commandEvent.command.options.size <= 1) {
            return PurchaseResult.Fail("Pour cet achat il faut spécifier un autre langage")
        }

        val otherLanguage = commandEvent.command.options.skip(1).joinToString(" ")
        bot.send(commandEvent.channel, "Tout le monde préfère Kotlin à $otherLanguage")

        return PurchaseResult.Success()
    }
}
