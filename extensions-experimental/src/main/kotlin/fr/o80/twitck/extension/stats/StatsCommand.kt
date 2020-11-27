package fr.o80.twitck.extension.stats

import fr.o80.twitck.lib.api.bean.event.CommandEvent
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
            "!stats" -> handleStatCommand(messenger, commandEvent)
        }
        return commandEvent
    }

    private fun handleStatCommand(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.size != 1) {
            messenger.sendImmediately(commandEvent.channel, "Usage !stat <name>")
            return
        }

        when (val stateName = commandEvent.command.options[0]) {
            "messages" -> handleMessagesStat(messenger, stateName, commandEvent.channel)
            "commands" -> handleCommandsStat(messenger, stateName, commandEvent.channel)
            else -> {
                // noop for now
            }
        }
    }

    private fun handleMessagesStat(messenger: Messenger, stateName: String, channel: String) {
        statsData.get(STATS_NAMESPACE, stateName)?.let { data ->
            val statCalculator = StatCalculator(data)
            val messagesCount = statCalculator.count()
            val minMsg = statCalculator.min(STAT_INFO_COUNT)?.let { "taille mini: $it" }
            val maxMsg = statCalculator.max(STAT_INFO_COUNT)?.let { "taille maxi: $it" }
            val avgMsg = statCalculator.avg(STAT_INFO_COUNT)?.let { "taille moyenne: $it" }
            val extremesMsg =
                if (minMsg != null && maxMsg != null)
                    "[$minMsg, $maxMsg, $avgMsg]"
                else
                    ""
            messenger.sendImmediately(channel, "Il y a eu $messagesCount messages $extremesMsg")
        }
    }

    private fun handleCommandsStat(messenger: Messenger, stateName: String, channel: String) {
        statsData.get(STATS_NAMESPACE, stateName)?.let { data ->
            val statCalculator = StatCalculator(data)

            statCalculator.countBy("command")
                .maxByOrNull { (_, v) -> v }
                ?.let { moreUsedCommand ->
                    messenger.sendImmediately(
                        channel,
                        "Command la plus utilisée : !${moreUsedCommand.key} ${moreUsedCommand.value}x"
                    )
                }
            // TODO OPZ Idée pour la prochaine fois, pouvoir savoir les commandes les plus utilisées par chaun de utilisateurs
            // TODO statCalculator.countBy(STAT_INFO_VIEWER, "command") =>
            //        { "giftsub" : [ {"claim":3040, "stat": 2}, "delphes": {"stat" : 43, "claim": 3} ] }
        }
    }

}
