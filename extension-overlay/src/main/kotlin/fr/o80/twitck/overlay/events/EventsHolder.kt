package fr.o80.twitck.overlay.events

import fr.o80.twitck.lib.api.extension.OverlayEvent
import java.util.*

class EventsHolder(
    private val maxSize: Int
) {

    private val _events = LinkedList<OverlayEvent>()

    val events: List<OverlayEvent> get() = _events.toList()

    fun record(event: OverlayEvent) {
        if (_events.size == maxSize)
            _events.pollFirst()
        _events.offerLast(event)
    }
}
