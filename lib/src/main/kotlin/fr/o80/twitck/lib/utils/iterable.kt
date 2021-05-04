package fr.o80.twitck.lib.utils

inline fun <T, R> Iterable<T>.foldUntilNull(initial: R, operation: (acc: R, T) -> R?): R? {
    var accumulator: R? = initial
    for (element in this) {
        if (accumulator == null) {
            break
        }
        accumulator = operation(accumulator, element)
    }
    return accumulator!!
}
