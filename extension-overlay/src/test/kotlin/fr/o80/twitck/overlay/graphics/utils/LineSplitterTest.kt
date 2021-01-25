package fr.o80.twitck.overlay.graphics.utils

import org.junit.Test
import kotlin.test.assertEquals

internal class LineSplitterTest {

    private val splitter = LineSplitter()

    @Test
    fun shouldTest() {
        // Given
        val longLine = "Olivier est en train de faire du découpage de grande lignes"

        // When
        val oneLines = splitter.split(longLine, 60)
        val twoLines = splitter.split(longLine, 29)
        val threeLines = splitter.split(longLine, 19)

        // then
        assertEquals(1, oneLines.size)
        assertEquals("Olivier est en train de faire du découpage de grande lignes", oneLines[0])

        assertEquals(2, twoLines.size)
        assertEquals("Olivier est en train de faire", twoLines[0])
        assertEquals("du découpage de grande lignes", twoLines[1])

        assertEquals(3, threeLines.size)
        assertEquals("Olivier est en train", threeLines[0])
        assertEquals("de faire du découpage", threeLines[1])
        assertEquals("de grande lignes", threeLines[2])
    }
}