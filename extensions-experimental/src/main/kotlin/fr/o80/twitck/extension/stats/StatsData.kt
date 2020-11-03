package fr.o80.twitck.extension.stats

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class StatsData {

    data class Key(val namespace: String, val name: String)

    private val dataLake: MutableMap<Key, List<Extra>> = mutableMapOf()

    private val lock = ReentrantReadWriteLock()

    fun hit(namespace: String, name: String, extra: Extra = mapOf()) {
        lock.write {
            dataLake.compute(Key(namespace, name)) { _, info ->
                if (info == null) {
                    listOf(extra)
                } else {
                    info + extra
                }
            }
        }
    }

    fun get(namespace: String): Map<String, List<Extra>> {
        return lock.read {
            dataLake.filter { (key, _) -> key.namespace == namespace }
                .mapKeys { (key, _) -> key.name }
        }
    }

    fun get(namespace: String, name: String): List<Extra>? {
        return lock.read { dataLake[Key(namespace, name)] }
    }

}
