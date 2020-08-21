package fr.o80.twitck.extension.points

class PointsBank {

    private val balances: MutableMap<String, Int> = mutableMapOf()

    fun getPoints(login: String): Int =
        balances.getOrDefault(login, 0)

    fun addPoints(login: String, points: Int) {
        balances.compute(login) { _, balance ->
            (balance ?: 0) + points
        }
    }

    fun removePoints(login: String, points: Int): Boolean =
        synchronized(balances) {
            if (canConsume(login, points)) {
                balances.computeIfPresent(login) { _, balance -> balance - points }
                true
            } else {
                false
            }
        }

    fun transferPoints(fromLogin: String, toLogin: String, points: Int): Boolean =
        synchronized(balances) {
            if (canConsume(fromLogin, points)) {
                balances.computeIfPresent(fromLogin) { _, balance -> balance - points }
                addPoints(toLogin, points)
                true
            } else {
                false
            }
        }

    private fun canConsume(login: String, points: Int): Boolean =
        balances.getOrDefault(login, 0) >= points
}