package fr.o80.twitck.lib.api.service.time

interface TimeChecker {
    fun executeIfNotCooldown(login: String, block: () -> Unit)
}
