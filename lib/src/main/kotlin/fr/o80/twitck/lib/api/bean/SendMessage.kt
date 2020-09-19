package fr.o80.twitck.lib.api.bean

import java.time.Duration

class SendMessage (
    val channel: String,
    val content: String,
    val deadline: Deadline,
    val coolDown: CoolDown? = null
)

sealed class Deadline {
    object Immediate : Deadline()
    class Postponed(
        val importance: Importance
    ) : Deadline()
}

enum class Importance(val value: Int) {
    HIGH(1), LOW(0)
}

class CoolDown(
    val duration: Duration
)
