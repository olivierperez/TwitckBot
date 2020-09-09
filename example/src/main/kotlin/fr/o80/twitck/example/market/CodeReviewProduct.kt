package fr.o80.twitck.example.market

import fr.o80.twitck.extension.market.Product
import fr.o80.twitck.extension.market.PurchaseResult
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.skip

object CodeReviewProduct : Product {

    override val name: String = "codereview"

    override fun computePrice(commandEvent: CommandEvent): Int? {
        val level = Level.fromValue(commandEvent.command.options[1])
        return level?.price
    }

    override fun execute(
        bot: TwitckBot,
        commandEvent: CommandEvent,
        logger: Logger,
        storageExtension: StorageExtension,
        serviceLocator: ServiceLocator
    ): PurchaseResult {
        val level = Level.fromValue(commandEvent.command.options[1]) ?: return PurchaseResult.Fail("Prix invalide!")
        val msg = commandEvent.command.options.skip(2).joinToString(" ")

        return PurchaseResult.WaitingValidation(
            login = commandEvent.login,
            code = "CodeReview ${level.code}",
            message = msg,
            price = level.price
        )
    }

    // silver => 10 min
    // gold => 30 min
    // diamond => 1h
    enum class Level(val code: String, val price: Int) {
        SILVER("silver", 1100),
        GOLD("gold", 3500),
        DIAMOND("diamond", 7500);

        companion object {
            fun fromValue(value: String): Level? {
                return values().firstOrNull { it.code == value }
            }
        }
    }

}