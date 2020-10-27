package fr.o80.twitck.extension.stats

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.service.Messenger

class StatsCommand(
    private val statsData: StatsData
) {
    // !stat commands:claim
    // !stat polls
    // !stat messages
    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!stat" -> handleStatCommand(messenger, commandEvent)
        }
        return commandEvent
    }

    private fun handleStatCommand(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.size != 1) {
            messenger.sendImmediately(commandEvent.channel, "Usage !stat <name>")
            return
        }

        val stateName = commandEvent.command.options[0]
        val stat = statsData.get("stats", stateName)
        if (stat != null) {
            messenger.sendImmediately(commandEvent.channel, "Il y a eu $stat $stateName")
        } else {
            messenger.sendImmediately(commandEvent.channel, "Aucune statistique pour le moment")
        }
    }
}