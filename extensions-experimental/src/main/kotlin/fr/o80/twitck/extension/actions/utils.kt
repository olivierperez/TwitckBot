package fr.o80.twitck.extension.actions

fun merge(
    first: ByteArray,
    second: ByteArray,
    third: ByteArray
): ByteArray {
    return ByteArray(first.size + second.size + third.size) { index ->
        when {
            index < first.size -> first[index]
            index < first.size + second.size -> second[index - first.size]
            else -> third[index - first.size - second.size]
        }
    }
}
