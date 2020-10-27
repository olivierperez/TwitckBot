package fr.o80.twitck.extension.stats

import fr.o80.twitck.lib.utils.update
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.max
import kotlin.math.min

class StatsData {

    private val storage: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()

    private val lock = ReentrantReadWriteLock()

    fun increment(namespace: String, key: String, count: Int) {
        increment(namespace, key, count.toLong())
    }

    fun increment(namespace: String, name: String, count: Long = 1) {
        lock.write {
            storage.update(namespace, name) { current -> (current ?: 0) + count }
        }
    }

    fun maximum(namespace: String, name: String, value: Long) {
        lock.write {
            storage.update(namespace, name) { current -> max(current ?: Long.MIN_VALUE, value) }
        }
    }

    fun minimum(namespace: String, name: String, value: Long) {
        lock.write {
            storage.update(namespace, name) { current -> min(current ?: Long.MAX_VALUE, value) }
        }
    }

    fun get(namespace: String): Map<String, Long>? {
        return lock.read { storage[namespace] }
    }

    fun get(namespace: String, name: String): Long? {
        return lock.read { storage[namespace]?.get(name) }
    }

}
