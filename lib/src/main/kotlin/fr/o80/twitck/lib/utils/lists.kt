package fr.o80.twitck.lib.utils

fun <E> List<E>.skip(count: Int): List<E> =
    if (count == 0) this
    else subList(count, size)
