package fr.o80.twitck.extension.actions

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import fr.o80.twitck.extension.actions.model.RemoteAction
import fr.o80.twitck.lib.api.extension.StorageExtension
import java.lang.reflect.Type

private const val KEY_ACTIONS = "actions"

class RemoteActionStore(
    private val storage: StorageExtension
) {

    private val namespace: String = RemoteActionStore::class.java.name

    private val moshi = Moshi.Builder().build()
    private val adapter: JsonAdapter<List<RemoteAction>>

    init {
        val type: Type =
            Types.newParameterizedType(MutableList::class.java, RemoteAction::class.java)
        adapter = moshi.adapter(type)
    }

    private val actions: MutableList<RemoteAction> = mutableListOf(
        RemoteAction("Yata !", "fallback.png", "Command:!yata"),
        RemoteAction("Youpi !", "fallback.png", "Command:!youpi"),
        RemoteAction("Ton écran !", "fallback.png", "Command:!screen"),
        RemoteAction("Soleil", "fallback.png", "Message:De toute choses, cherche le côté soleil."),
    )

    fun getActions(): List<RemoteAction> {
        return storage.getGlobalInfo(namespace)
            .firstOrNull { it.first == KEY_ACTIONS }?.second
            ?.let { adapter.fromJson(it) }
            ?: listOf()
    }

    fun addAction(action: RemoteAction) {
        val actions = storage.getGlobalInfo(namespace)
            .firstOrNull { it.first == KEY_ACTIONS }?.second
            ?.let { adapter.fromJson(it)!!.toMutableList() }
            ?: mutableListOf()

        actions += action

        storage.putGlobalInfo(namespace, KEY_ACTIONS, adapter.toJson(actions))
    }
}
