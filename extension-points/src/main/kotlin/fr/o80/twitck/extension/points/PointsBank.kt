package fr.o80.twitck.extension.points

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.utils.tryToInt

class PointsBank(
    private val extensionProvider: ExtensionProvider
) {

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = DefaultPointsExtension::class.java.name

    private val lock = Any()

    fun getPoints(login: String): Int =
        storage.getPoints(login)

    fun addPoints(login: String, points: Int) {
        synchronized(lock) {
            val currentPoints = storage.getPoints(login)
            val newPoints = currentPoints + points
            storage.putPoints(login, newPoints)
        }
    }

    fun removePoints(login: String, points: Int): Boolean =
        synchronized(lock) {
            if (canConsume(login, points)) {
                val currentPoints = storage.getPoints(login)
                val newPoints = currentPoints - points
                storage.putPoints(login, newPoints)
                true
            } else {
                false
            }
        }

    fun transferPoints(fromLogin: String, toLogin: String, points: Int): Boolean =
        synchronized(lock) {
            if (canConsume(fromLogin, points)) {
                removePoints(fromLogin, points)
                addPoints(toLogin, points)
                true
            } else {
                false
            }
        }

    private fun canConsume(login: String, points: Int): Boolean =
        storage.getPoints(login) >= points

    private fun StorageExtension.getPoints(login: String) =
        getUserInfo(login, namespace, "balance").tryToInt() ?: 0

    private fun StorageExtension.putPoints(login: String, points: Int) =
        putUserInfo(login, namespace, "balance", points.toString())
}