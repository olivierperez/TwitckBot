package fr.o80.twitck.lib.api.service

import kotlin.test.Test
import kotlin.test.assertEquals


class TimeParserTest {

    private val parser = TimeParser()

    @Test
    fun `should fail to parse`() {
        assertEquals(-1, parser.parse(""))
        assertEquals(-1, parser.parse("aze"))
        assertEquals(-1, parser.parse("125houm"))
    }

    @Test
    fun `should parse without unit`() {
        assertEquals(50, parser.parse("50"))
        assertEquals(42, parser.parse("42"))
        assertEquals(125, parser.parse("125"))
    }

    @Test
    fun `should parse with seconds unit`() {
        assertEquals(50, parser.parse("50s"))
        assertEquals(42, parser.parse("42sec"))
        assertEquals(125, parser.parse("125seconds"))
    }

    @Test
    fun `should parse with minutes unit`() {
        assertEquals(50 * 60, parser.parse("50m"))
        assertEquals(42 * 60, parser.parse("42min"))
        assertEquals(125 * 60, parser.parse("125minutes"))
    }

    @Test
    fun `should parse with hours unit`() {
        assertEquals(50 * 3600, parser.parse("50h"))
        assertEquals(42 * 3600, parser.parse("42hour"))
        assertEquals(125 * 3600, parser.parse("125hours"))
    }

    @Test
    fun `should parse with minutes and seconds units`() {
        assertEquals(1 * 60 + 50, parser.parse("1m50s"))
        assertEquals(2 * 60 + 42, parser.parse("2m42s"))
        assertEquals(3 * 60 + 25, parser.parse("3m25s"))
    }

    @Test
    fun `should parse with hours and seconds units`() {
        assertEquals(1 * 3600 + 50, parser.parse("1h50s"))
        assertEquals(2 * 3600 + 42, parser.parse("2h42s"))
        assertEquals(3 * 3600 + 25, parser.parse("3h25s"))
    }

    @Test
    fun `should parse with hours and minutes units`() {
        assertEquals(1 * 3600 + 50 * 60, parser.parse("1h50m"))
        assertEquals(2 * 3600 + 42 * 60, parser.parse("2h42m"))
        assertEquals(3 * 3600 + 25 * 60, parser.parse("3h25m"))
    }

    @Test
    fun `should parse with hours and minutes and seconds units`() {
        assertEquals(1 * 3600 + 10 * 60 + 50, parser.parse("1h10m50s"))
        assertEquals(3 * 3600 + 2 * 60 + 42, parser.parse("3h2m42s"))
        assertEquals(10 * 3600 + 37 * 60 + 25, parser.parse("10h37m25s"))
    }
}