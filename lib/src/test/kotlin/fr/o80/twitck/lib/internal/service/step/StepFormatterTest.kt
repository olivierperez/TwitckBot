package fr.o80.twitck.lib.internal.service.step

import fr.o80.twitck.lib.api.service.step.StepParams
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StepFormatterTest {

    private val formatter = StepFormatter()

    @Test
    fun `should format with Viewer name only`() {
        // Given
        val input = "--#USER#--"
        val param = StepParams("", "Olivier", emptyList())

        // When
        val output = formatter.format(input, param)

        // Then
        assertEquals("--Olivier--", output)
    }

    @Test
    fun `should format with full-length params`() {
        // Given
        val input = "--#PARAMS#--"
        val param = StepParams("", "", listOf("A", "B", "C"))

        // When
        val output = formatter.format(input, param)

        // Then
        assertEquals("--A B C--", output)
    }

    @Test
    fun `should format with indexed param`() {
        // Given
        val input = "--#PARAM-0#+#PARAM-2#--"
        val param = StepParams("", "", listOf("A", "B", "C"))

        // When
        val output = formatter.format(input, param)

        // Then
        assertEquals("--A+C--", output)
    }

    @Test
    fun `should format with bits`() {
        // Given
        val input = "$$#BITS#$$"
        val param = StepParams("", "", emptyList(), 13)

        // When
        val output = formatter.format(input, param)

        // Then
        assertEquals("$$13$$", output)
    }

}
