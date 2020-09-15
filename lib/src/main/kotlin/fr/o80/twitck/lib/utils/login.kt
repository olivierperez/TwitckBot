package fr.o80.twitck.lib.utils

fun String.sanitizeLogin() = this.removePrefix("@").toLowerCase()
