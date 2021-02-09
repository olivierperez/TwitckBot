package fr.o80.twitck.overlay

import fr.o80.twitck.lib.api.extension.OverlayEvent
import java.util.*

class EventsHolder {

    private val _events = LinkedList<OverlayEvent>()

    val events: List<OverlayEvent> get() = _events.toList()

    fun record(event: OverlayEvent) {
        _events.offerLast(event)
    }
}
