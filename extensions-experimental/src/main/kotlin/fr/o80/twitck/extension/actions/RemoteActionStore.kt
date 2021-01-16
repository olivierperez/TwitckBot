package fr.o80.twitck.extension.actions

class RemoteActionStore {

    private val actions: MutableList<RemoteAction> = mutableListOf(
        RemoteAction("Yata!", "olivier.png", "!yata")
    )

    fun getActions(): List<RemoteAction> {
        return actions
    }

    fun addAction(action: RemoteAction) {
        actions += action
    }
}
