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
    object Repeated : Deadline()
}

enum class Importance {
    HIGH, LOW
}

class CoolDown(
    val duration: Duration
)
