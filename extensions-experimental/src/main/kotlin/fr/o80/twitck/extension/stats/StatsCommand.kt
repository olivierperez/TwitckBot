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
            val minMsg = stat.takeIf { it.min < Long.MAX_VALUE }?.let { "min: ${it.min}" }
            val maxMsg = stat.takeIf { it.max > Long.MIN_VALUE }?.let { "max: ${it.max}" }
            val extremesMsg = if (minMsg != null && maxMsg != null) "[$minMsg, $maxMsg]" else ""
            messenger.sendImmediately(commandEvent.channel, "Il y a eu ${stat.count} $stateName $extremesMsg")
        } else {
            messenger.sendImmediately(commandEvent.channel, "Aucune statistique pour le moment")
        }
    }
}