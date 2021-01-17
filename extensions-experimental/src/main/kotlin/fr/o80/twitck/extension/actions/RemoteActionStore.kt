package fr.o80.twitck.extension.actions

class RemoteActionStore {

    private val actions: MutableList<RemoteAction> = mutableListOf(
        RemoteAction("Yata !", "olivier.png", "Command:!yata"),
        RemoteAction("Youpi !", "olivier.png", "Command:!youpi"),
        RemoteAction("Ton écran !", "olivier.png", "Command:!screen"),
        RemoteAction("Soleil", "olivier.png", "Message:De toute choses, cherche le côté soleil."),
    )

    fun getActions(): List<RemoteAction> {
        return actions
    }

    fun addAction(action: RemoteAction) {
        actions += action
    }
}
