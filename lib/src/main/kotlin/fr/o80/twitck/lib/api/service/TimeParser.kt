package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.utils.tryToLong

private const val HOURS = "h(?:our(?:s)?)?"
private const val MINUTES = "m(?:in(?:ute(?:s)?)?)?"
private const val SECONDS = "s(?:ec(?:ond(?:s)?)?)?"

class TimeParser {

    private val simpleDuration = "^\\d+$".toRegex()

    private val complexDuration = "^(?:(\\d+)$HOURS)?(?:(\\d+)$MINUTES)?(?:(\\d+)$SECONDS)?$".toRegex()

    /**
     * Try to parse the given [input] as a human-readable time, but return -1 if it failed.
     */
    fun parse(input: String): Long {
        return if (simpleDuration.matches(input)) {
            input.toLong()
        } else {
            tryToParseComplexInput(input) ?: -1
        }
    }

    private fun tryToParseComplexInput(input: String): Long? {
        return complexDuration.matchEntire(input)?.takeIf { it.groupValues[0].isNotBlank() }?.let { matchResult ->
            val hours = matchResult.groupValues[1].tryToLong() ?: 0
            val minutes = matchResult.groupValues[2].tryToLong() ?: 0
            val seconds = matchResult.groupValues[3].tryToLong() ?: 0
            hours * 3600 + minutes * 60 + seconds
        }
    }
}