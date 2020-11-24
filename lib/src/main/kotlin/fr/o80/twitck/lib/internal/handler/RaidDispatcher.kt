package fr.o80.twitck.lib.internal.handler

import fr.o80.twitck.lib.api.bean.RaidEvent
import fr.o80.twitck.lib.api.handler.RaidHandler
import fr.o80.twitck.lib.api.service.Messenger

class RaidDispatcher(
    private val messenger: Messenger,
    private val handlers: List<RaidHandler>
) {
    fun dispatch(raidEvent: RaidEvent) {
        handlers.fold(raidEvent) { acc, handler ->
            handler(messenger, acc)
        }
    }
}
