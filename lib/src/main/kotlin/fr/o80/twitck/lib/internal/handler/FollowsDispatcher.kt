package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.event.FollowsEvent
import fr.o80.twitck.lib.api.handler.FollowsHandler
import fr.o80.twitck.lib.api.service.Messenger

class FollowsDispatcher(
    private val messenger: Messenger,
    private val handlers: List<FollowsHandler>
) {
    fun dispatch(follows: FollowsEvent) {
        handlers.fold(follows) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
