package fr.o80.twitck.extension.stats

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class StatCalculatorTest {

    private val calculator = StatCalculator(
        listOf(
            mapOf(
                "command" to "A",
                "toto" to 30
            ),
            mapOf(
                "command" to "A",
                "toto" to 10
            ),
            mapOf(
                "command" to "B",
                "toto" to 10
            ),
            mapOf(
                "command" to "C",
                "toto" to 0
            ),
            mapOf(
                "another thing" to 0
            )
        )
    )

    @Test
    fun `should count hits`() {
        assertEquals(5, calculator.count())
    }

    @Test
    fun `should do math with toto the right way`() {
        assertEquals(0, calculator.min("toto"))
        assertEquals(30, calculator.max("toto"))
        assertEquals(12.5f, calculator.avg("toto"))
    }

    @Test
    fun `should do math with another thing the right way`() {
        assertEquals(0, calculator.min("another thing"))
        assertEquals(0, calculator.max("another thing"))
        assertEquals(0f, calculator.avg("another thing"))
    }

    @Test
    fun `should not do math with nothing`() {
        assertNull(calculator.min("nothing"))
        assertNull(calculator.max("nothing"))
        assertNull(calculator.avg("nothing"))
    }

    @Test
    fun `should count by Command`() {
        val countByCommand = calculator.countBy("command")
        assertEquals(2, countByCommand["A"])
        assertEquals(1, countByCommand["B"])
        assertEquals(1, countByCommand["C"])
        assertNull(countByCommand["Non used command"])
    }

}
