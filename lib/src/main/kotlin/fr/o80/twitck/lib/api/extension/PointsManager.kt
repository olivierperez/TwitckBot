package fr.o80.twitck.lib.api.extension

interface PointsManager {
    val channel: String
    fun getPoints(login: String): Int
    fun addPoints(login: String, points: Int) // TODO OPZ Si cette méthode gère le multiplicateur, add(5) puis remove(5) peut produire un résultat étonant (!= 0)
    fun removePoints(login: String, points: Int): Boolean
    fun transferPoints(fromLogin: String, toLogin: String, points: Int): Boolean
}