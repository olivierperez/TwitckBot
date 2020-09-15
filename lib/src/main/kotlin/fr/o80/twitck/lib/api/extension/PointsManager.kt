package fr.o80.twitck.lib.api.extension

interface PointsManager {
    val channel: String
    fun getPoints(login: String): Int
    fun addPoints(login: String, points: Int)
    fun consumePoints(login: String, points: Int): Boolean
}