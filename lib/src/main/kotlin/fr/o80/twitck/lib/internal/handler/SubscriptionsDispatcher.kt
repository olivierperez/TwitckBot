package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.SubscriptionEvent
import fr.o80.twitck.lib.api.handler.SubscriptionsHandler
import fr.o80.twitck.lib.api.service.Messenger

class SubscriptionsDispatcher(
    private val messenger: Messenger,
    private val handlers: List<SubscriptionsHandler>
) {
    fun dispatch(follow: SubscriptionEvent) {
        handlers.fold(follow) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
