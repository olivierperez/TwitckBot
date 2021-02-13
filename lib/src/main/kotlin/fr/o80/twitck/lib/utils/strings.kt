package fr.o80.twitck.lib.utils

fun String.addPrefix(prefix: String): String {
    return if (this.startsWith(prefix)) this
    else prefix + this
}
