package fr.o80.twitck.lib.api.service

interface CommandTriggering {
    fun sendCommand(command: String)
}

interface CommandsFromExtension {
    var listener: (String) -> Unit
}

internal class CommandTriggeringImpl : CommandTriggering, CommandsFromExtension {

    override var listener: (String) -> Unit = {}

    override fun sendCommand(command: String) {
        listener(command)
    }

}
