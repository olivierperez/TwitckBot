package fr.o80.twitck.extension.stats

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import java.time.Duration

private const val DEBUG = "debug"
private const val VIEWER = "viewer"
private const val COMMANDS = "commands"
private const val MESSAGES = "messages"

class StatsCommand(
    private val statsData: StatsData,
    private val logger: Logger
) {
    // !stat commands:claim
    // !stat polls
    // !stat messages
    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!stat", "!stats" -> handleStatCommand(messenger, commandEvent)
        }
        return commandEvent
    }

    private fun handleStatCommand(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.isEmpty()) {
            messenger.sendImmediately(
                commandEvent.channel,
                "Usage !stat <name> [possibly more options]"
            )
            return
        }

        when (commandEvent.command.options[0]) {
            MESSAGES -> handleMessagesStat(messenger, commandEvent.channel)
            COMMANDS -> handleCommandsStat(messenger, commandEvent.channel)
            VIEWER -> handleViewerStat(messenger, commandEvent)
            DEBUG -> handleDebugCommand(commandEvent)
            else -> {
                // noop for now
            }
        }
    }

    private fun handleMessagesStat(messenger: Messenger, channel: String) {
        statsData.get(STATS_NAMESPACE, MESSAGES)?.let { data ->
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

    private fun handleCommandsStat(messenger: Messenger, channel: String) {
        statsData.get(STATS_NAMESPACE, COMMANDS)?.let { data ->
            val statCalculator = StatCalculator(data)

            statCalculator.countBy(STAT_INFO_COMMAND)
                .maxByOrNull { (_, v) -> v }
                ?.let { moreUsedCommand ->
                    messenger.sendImmediately(
                        channel,
                        "Command la plus utilisée : !${moreUsedCommand.key} ${moreUsedCommand.value}x"
                    )
                }
        }
    }

    private fun handleViewerStat(messenger: Messenger, commandEvent: CommandEvent) {
        statsData.get(STATS_NAMESPACE).let { data ->
            val statCalculator = GroupingStatCalculator(data)

            val login = commandEvent.command.options.takeIf { it.size >= 2 }?.get(1)
                ?.toLowerCase()
                ?: commandEvent.viewer.login

            val viewerCommandsStats = statCalculator.countBy(
                STAT_INFO_VIEWER,
                STAT_INFO_COMMAND
            )[login]

            if (viewerCommandsStats == null) {
                messenger.sendImmediately(
                    commandEvent.channel,
                    "Pas de stats pour $login",
                    CoolDown(Duration.ofSeconds(30))
                )
            } else {
                viewerCommandsStats.maxByOrNull { (_, count) -> count }
                    ?.let {
                        messenger.sendImmediately(
                            commandEvent.channel,
                            "La command la plus utilisée par $login est !${it.key} ${it.value}x",
                            CoolDown(Duration.ofSeconds(30))
                        )
                    }
            }
        }
    }

    private fun handleDebugCommand(commandEvent: CommandEvent) {
        if (Badge.BROADCASTER !in commandEvent.viewer.badges)
            return

        logger.debug("\n---- Stats ----\n${statsData.getDebug()}\n---------------")
    }

}
