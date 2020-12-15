package fr.o80.twitck.extension.stats

class StatCalculator(
    private val extras: List<Hit>
) {

    fun count(): Int {
        return extras.size
    }

    fun min(infoName: String): Int? {
        return extras
            .filter { it[infoName] is Int }
            .minOfOrNull { it[infoName] as Int }
    }

    fun max(infoName: String): Int? {
        return extras
            .asSequence()
            .filter { it[infoName] is Int }
            .maxOfOrNull { it[infoName] as Int }
    }

    fun avg(infoName: String): Float? {
        val info = extras
            .filter { it[infoName] is Int }

        return if (info.isNullOrEmpty()) {
            null
        } else {
            info.sumOf { it[infoName] as Int } / info.size.toFloat()
        }
    }

    fun countBy(infoName: String): Map<Any, Int> {
        return extras
            .filter { hitExtra -> hitExtra.containsKey(infoName) }
            .groupBy { hitExtra -> hitExtra.getValue(infoName) }
            .mapValues { (_, value) -> value.size }
    }

}
