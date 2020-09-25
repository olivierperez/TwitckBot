package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.PostponedMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class PostponedMessageComparatorTest {

    @Test
    fun `Comparator should help to sorted SendMessage(s)`() {
        sortedSetOf(PostponedMessageComparator).apply {
            add(PostponedMessage("1", "HIGH", Importance.HIGH))
            add(PostponedMessage("4", "LOW", Importance.LOW))
            add(PostponedMessage("5", "HIGH", Importance.HIGH))
        }.toList().let { list ->
            assertEquals("HIGH", list[0].content)
            assertEquals("HIGH", list[1].content)
            assertEquals("LOW", list[2].content)
        }
    }
}
