package fr.o80.twitck.lib.api.bean

import java.time.Duration

class PostponedMessage(
    val channel: String,
    val content: String,
    val importance: Importance
)

enum class Importance(val value: Int) {
    HIGH(1), LOW(0)
}

class CoolDown(
    val duration: Duration
) {
    companion object {
        fun ofSeconds(seconds: Long): CoolDown {
            return CoolDown(Duration.ofSeconds(seconds))
        }
    }
}
