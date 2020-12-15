package fr.o80.twitck.extension.stats

class GroupingStatCalculator(
    private val data: Map<String, List<Hit>>
) {

    /*
    commands: [
        {viewer:toto, command:stats},
        {viewer:toto, command:stats},
        {viewer:toto, command:help},
        {viewer:plop, command:claim},
    ],
    messages: [
        {viewer:toto, message:pipopipo},
    ]





    [
        toto : [stats, stats, help]
        plop: [claim]
    ]
    */
//    fun groupBy(namespace: String, name: String): Any? {
//        return dataLake
//                .filter { (key, _) -> key.namespace == namespace }
//                .flatMap { (_, extras) -> extras }
//                .groupBy { it[name] }
//    }

    fun groupBy(name: String): Map<Any?, List<Hit>> {
        return data
            .flatMap { it.value }
            .filter { it.isNotEmpty() }
            .groupBy { it[name] }
    }

    fun countBy(firstGroup: String, countBy: String): Map<Any?, Map<Any?, Int>> {
        return data// TODO sequence
            .filter { it.value.isNotEmpty() }
            .flatMap { it.value }
            .groupBy { it[firstGroup] }
            .mapValues { (_, extras) -> extras.filter { it.containsKey(countBy) } }
            .mapValues { (_, extras) -> extras.map { it[countBy] } }
            .mapValues { (_, commands) -> commands.groupingBy { it }.eachCount() }
    }

}
