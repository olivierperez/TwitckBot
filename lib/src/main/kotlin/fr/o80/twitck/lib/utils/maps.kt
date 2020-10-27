package fr.o80.twitck.lib.utils

fun <X, Y, V> MutableMap<X, MutableMap<Y, V>>.update(
    namespace: X,
    name: Y,
    update: (V?) -> V
) {
    compute(namespace) { _, namespaceStats ->
        namespaceStats
            ?.apply { compute(name) { _, current -> update(current) } }
            ?: mutableMapOf()
    }
}
