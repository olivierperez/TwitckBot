package fr.o80.twitck.poll

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.Deadline
import fr.o80.twitck.lib.api.bean.SendMessage
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.TimeParser
import java.util.Timer
import kotlin.concurrent.schedule

class PollCommands(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val messages: Messages,
    private val pointsForEachVote: Int,
    private val extensionProvider: ExtensionProvider,
    private val timeParser: TimeParser = TimeParser()
) {

    private var currentPoll: CurrentPoll? = null

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            // !poll 120 Est-ce que je dois changer de langage ?
            "!poll" -> handlePoll(messenger, commandEvent)
            // !vote Non
            "!vote" -> handleVote(commandEvent)
        }

        return commandEvent
    }

    private fun handlePoll(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.badges.none { badge -> badge in privilegedBadges }) return

        if (commandEvent.command.options.isNotEmpty()) {
            startPoll(messenger, commandEvent)
        } else {
            showResult(messenger)
        }
    }

    private fun handleVote(commandEvent: CommandEvent) {
        val vote = commandEvent.command.options.joinToString(" ").toLowerCase()
        val voteResult = currentPoll?.addVote(commandEvent.login, vote)

        if (voteResult == Vote.NEW_VOTE && pointsForEachVote > 0) {
            extensionProvider.forEach(PointsManager::class) { pointsManager ->
                pointsManager.addPoints(commandEvent.login, pointsForEachVote)
            }
        }
    }

    private fun startPoll(messenger: Messenger, commandEvent: CommandEvent) {
        val command = commandEvent.command
        if (command.options.size < 2) {
            messenger.send(SendMessage(channel, messages.errorCreationPollUsage, Deadline.Immediate))
            return
        }

        val seconds = timeParser.parse(command.options[0])
        val title = command.options.subList(1, command.options.size).joinToString(" ")

        if (seconds == -1L) {
            messenger.send(SendMessage(channel, messages.errorDurationIsMissing, Deadline.Immediate))
            return
        }

        currentPoll = CurrentPoll(title)
        messenger.send(SendMessage(channel, messages.newPoll.replace("#TITLE#", title), Deadline.Immediate))

        Timer().schedule(seconds * 1000) {
            currentPoll?.let { poll ->
                val resultMsg = generateResultMessage(poll, messages.pollHasJustFinished)
                messenger.send(SendMessage(channel, resultMsg, Deadline.Immediate))
                currentPoll = null
            }
        }
    }

    private fun showResult(messenger: Messenger) {
        currentPoll?.let { poll ->
            val resultMsg = generateResultMessage(poll, messages.currentPollResult)
            messenger.send(SendMessage(channel, resultMsg, Deadline.Immediate))
        }
    }

    internal fun generateResultMessage(poll: CurrentPoll, globalResultFormat: String): String {
        val bestResults = poll.getBestResults(5)

        return if (bestResults.isNotEmpty()) {
            val resultsMsg = bestResults.joinToString(", ") {
                messages.oneResultFormat
                    .replace("#ANSWER#", it.first)
                    .replace("#COUNT#", it.second.toString())
            }
            globalResultFormat
                .replace("#TITLE#", poll.title)
                .replace("#RESULTS#", resultsMsg)
        } else {
            messages.pollHasNoVotes.replace("#TITLE#", poll.title)
        }
    }

}