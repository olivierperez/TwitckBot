package fr.o80.twitck.extension.stats

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.max
import kotlin.math.min

class StatsData {

    data class Key(val namespace: String, val name: String)
    data class Info(val count: Long, val min: Long, val max: Long, val extras: Map<String, Any>)// TODO OPZ extras devrait être une list de Map<String, Any>

    private val storage: MutableMap<Key, Info> = mutableMapOf()

    private val lock = ReentrantReadWriteLock()

    fun increment(namespace: String, name: String, vararg extra: Pair<String, Any>) {
        increment(namespace, name, 1L, *extra)
    }

    fun increment(namespace: String, name: String, count: Int, vararg extra: Pair<String, Any>) {
        increment(namespace, name, count.toLong(), *extra)
    }

    fun increment(namespace: String, name: String, count: Long, vararg extra: Pair<String, Any>) {
        lock.write {
            storage.compute(Key(namespace, name)) { _, info ->
                (info?.copy(count = info.count + count, extras = extra.toMap())
                    ?: createDefaultInfo(count, extra))
            }
        }
    }

    fun maximum(namespace: String, name: String, value: Int, vararg extra: Pair<String, Any>) {
        maximum(namespace, name, value.toLong(), *extra)
    }

    fun maximum(namespace: String, name: String, value: Long, vararg extra: Pair<String, Any>) {
        lock.write {
            storage.compute(Key(namespace, name)) { _, info ->
                info?.copy(max = max(info.max, value), extras = extra.toMap())
                    ?: createDefaultInfo(1L, extra)
            }
        }
    }

    fun minimum(namespace: String, name: String, value: Int, vararg extra: Pair<String, Any>) {
        minimum(namespace, name, value.toLong(), *extra)
    }

    fun minimum(namespace: String, name: String, value: Long, vararg extra: Pair<String, Any>) {
        lock.write {
            storage.compute(Key(namespace, name)) { _, info ->
                info?.copy(min = min(info.min, value), extras = extra.toMap())
                    ?: createDefaultInfo(1L, extra)
            }
        }
    }

    fun get(namespace: String): Map<String, Info> {
        return lock.read {
            storage.filter { (key, _) -> key.namespace == namespace }
                .mapKeys { (key, _) -> key.name }
        }
    }

    fun get(namespace: String, name: String): Info? {
        return lock.read { storage[Key(namespace, name)] }
    }

    private fun createDefaultInfo(count: Long, extra: Array<out Pair<String, Any>>): Info =
        Info(
            count = count,
            min = Long.MAX_VALUE,
            max = Long.MIN_VALUE,
            extras = extra.toMap()
        )

}
