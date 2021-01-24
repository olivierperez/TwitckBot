package fr.o80.twitck.lib.api.service.time

interface TimeChecker {
    fun executeIfNotCooldown(login: String, block: () -> Unit): TimeFallback
}

interface TimeFallback {
    fun fallback(block: () -> Unit)
}

internal object NoOpFallback : TimeFallback {
    override fun fallback(block: () -> Unit) {
    }
}

internal object DoFallback : TimeFallback {
    override fun fallback(block: () -> Unit) {
        block()
    }
}
