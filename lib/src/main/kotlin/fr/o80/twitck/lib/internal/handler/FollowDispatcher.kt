package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.FollowEvent
import fr.o80.twitck.lib.api.handler.FollowHandler
import fr.o80.twitck.lib.api.service.Messenger

class FollowDispatcher(
    private val messenger: Messenger,
    private val handlers: List<FollowHandler>
) {
    fun dispatch(follow: FollowEvent) {
        handlers.fold(follow) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
