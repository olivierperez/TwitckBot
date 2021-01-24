package fr.o80.twitck.lib.api.service.time

import fr.o80.twitck.lib.api.extension.StorageExtension
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

class StorageFlagTimeChecker(
    private val storage: StorageExtension,
    private val namespace: String,
    private val flag: String,
    private val interval: Duration,
    private val now: () -> LocalDateTime = { LocalDateTime.now() }
) : TimeChecker {

    override fun executeIfNotCooldown(login: String, block: () -> Unit): TimeFallback {
        return if (couldExecute(login)) {
            handled(login)
            block()
            NoOpFallback
        } else {
            DoFallback
        }
    }

    internal fun couldExecute(login: String): Boolean {
        val timeOfLastOccurrence = storage.getUserInfo(login, namespace, flag)?.parse() ?: return true
        val durationSinceLastOccurrence = Duration.between(timeOfLastOccurrence, now())

        return durationSinceLastOccurrence > interval
    }

    internal fun handled(login: String) {
        storage.putUserInfo(login, namespace, flag, now().format())
    }

    private fun String.parse(): LocalDateTime? {
        return LocalDateTime.parse(this, FORMATTER)
    }

    private fun LocalDateTime.format(): String {
        return this.format(FORMATTER)
    }

}
