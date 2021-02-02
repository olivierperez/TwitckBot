package fr.o80.twitck.lib.api.service

interface CommandTriggering {
    fun sendCommand(command: String, options: List<String>)
}

interface CommandsFromExtension {
    var listener: (String, List<String>) -> Unit
}

internal class CommandTriggeringImpl : CommandTriggering, CommandsFromExtension {

    override var listener: (String, List<String>) -> Unit = { _, _ -> }

    override fun sendCommand(command: String, options: List<String>) {
        listener(command, options)
    }

}
