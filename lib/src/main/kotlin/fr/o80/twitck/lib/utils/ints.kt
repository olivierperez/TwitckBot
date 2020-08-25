package fr.o80.twitck.lib.utils

fun String?.tryToInt(): Int? {
    return this?.takeIf { it.matches("\\d+".toRegex()) }?.toInt()
}
