package fr.o80.twitck.extension.stats

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class StatsData {

    data class Key(val namespace: String, val name: String)

    private val dataLake: MutableMap<Key, List<Hit>> = mutableMapOf()

    private val lock = ReentrantReadWriteLock()

    fun hit(namespace: String, name: String, hit: Hit = mapOf()) {
        lock.write {
            dataLake.compute(Key(namespace, name)) { _, info ->
                if (info == null) {
                    listOf(hit)
                } else {
                    info + hit
                }
            }
        }
    }

    fun get(namespace: String): Map<String, List<Hit>> {
        return lock.read {
            dataLake.filter { (key, _) -> key.namespace == namespace }
                .mapKeys { (key, _) -> key.name }
        }
    }

    fun get(namespace: String, name: String): List<Hit>? {
        return lock.read { dataLake[Key(namespace, name)] }
    }

    fun getDebug(): String {
        return lock.read {
            dataLake.map {
                val (namespace, name) = it.key
                val extras = it.value

                StringBuilder()
                    .append("$namespace::$name\n")
                    .append(
                        extras.joinToString(
                            "\n |----------\n"
                        ) { extra -> debugExtra(extra) })
                    .toString()
            }.joinToString("\n=================================\n")
        }
    }

    private fun debugExtra(extra: Map<String, Any>): String {
        return extra.entries.joinToString("\n") { (k, v) -> " | $k: $v" }
    }

}
