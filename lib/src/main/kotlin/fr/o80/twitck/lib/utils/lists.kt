package fr.o80.twitck.lib.utils

fun <E> List<E>.skip(count: Int): List<E> = subList(count, size)
