package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.Deadline
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.SendMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class SendMessageComparatorTest {

    @Test
    fun `Comparator should help to sorted SendMessage(s)`() {
        sortedSetOf(SendMessageComparator).apply {
            add(SendMessage("1", "HIGH", Deadline.Postponed(Importance.HIGH)))
            add(SendMessage("2", "Repeated", Deadline.Repeated))
            add(SendMessage("3", "Immediate", Deadline.Immediate))
            add(SendMessage("4", "LOW", Deadline.Postponed(Importance.LOW)))
            add(SendMessage("5", "HIGH", Deadline.Postponed(Importance.HIGH)))
        }.toList().let { list ->
            assertEquals("Immediate", list[0].content)
            assertEquals("HIGH", list[1].content)
            assertEquals("HIGH", list[2].content)
            assertEquals("LOW", list[3].content)
            assertEquals("Repeated", list[4].content)
        }
    }
}
