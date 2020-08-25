package fr.o80.twitck.lib.utils

fun String?.tryToInt(): Int? {
    return this?.takeIf { it.matches("\\d+".toRegex()) }?.toInt()
}

fun String?.tryToLong(): Long? {
    return this?.takeIf { it.matches("\\d+".toRegex()) }?.toLong()
}
