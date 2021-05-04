package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.event.RewardEvent
import fr.o80.twitck.lib.api.handler.RewardHandler
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.utils.foldUntilNull

internal class RewardDispatcher(
    private val messenger: Messenger,
    private val handlers: List<RewardHandler>
) {
    fun dispatch(rewardEvent: RewardEvent) {
        handlers.foldUntilNull(rewardEvent) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
