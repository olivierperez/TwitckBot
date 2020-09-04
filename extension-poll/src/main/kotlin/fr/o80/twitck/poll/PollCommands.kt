package fr.o80.twitck.poll

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.PointsManager
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

    fun interceptCommandEvent(bot: TwitckBot, commandEvent: CommandEvent): CommandEvent {
        if (channel != commandEvent.channel)
            return commandEvent

        when (commandEvent.command.tag) {
            // !poll 120 Est-ce que je dois changer de langage ?
            "!poll" -> handlePoll(bot, commandEvent)
            // !vote Non
            "!vote" -> handleVote(commandEvent)
        }

        return commandEvent
    }

    private fun handlePoll(bot: TwitckBot, commandEvent: CommandEvent) {
        if (commandEvent.badges.none { badge -> badge in privilegedBadges }) return

        if (commandEvent.command.options.isNotEmpty()) {
            startPoll(bot, commandEvent)
        } else {
            showResult(bot)
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

    private fun startPoll(bot: TwitckBot, commandEvent: CommandEvent) {
        val command = commandEvent.command
        if (command.options.size < 2) {
            bot.send(channel, messages.errorCreationPollUsage)
            return
        }

        val seconds = timeParser.parse(command.options[0])
        val title = command.options.subList(1, command.options.size).joinToString(" ")

        if (seconds == -1L) {
            bot.send(channel, messages.errorDurationIsMissing)
            return
        }

        currentPoll = CurrentPoll(title)
        bot.send(channel, messages.newPoll.replace("#TITLE#", title))

        Timer().schedule(seconds * 1000) {
            currentPoll?.let { poll ->
                val best = poll.best

                if (best.second >= 1) {
                    val resultMsg = messages.pollHasJustFinished
                        .replace("#TITLE#", poll.title)
                        .replace("#BEST#", best.first)
                        .replace("#COUNT#", best.second.toString())
                    bot.send(channel, resultMsg)
                } else {
                    bot.send(channel, messages.pollHasNoVotes.replace("#TITLE#", poll.title))
                }
                currentPoll = null
            }
        }
    }

    private fun showResult(bot: TwitckBot) {
        currentPoll?.let { poll ->
            val best = poll.best
            if (best.second >= 1) {
                val resultMsg = messages.currentPollResult
                    .replace("#TITLE#", poll.title)
                    .replace("#BEST#", best.first)
                    .replace("#COUNT#", best.second.toString())
                bot.send(channel, resultMsg)
            } else {
                bot.send(channel, messages.pollHasNoVotes.replace("#TITLE#", poll.title))
            }
        }
    }

}

class CurrentPoll(
    val title: String
) {

    private val votes: MutableMap<String, String> = mutableMapOf()

    val best: Pair<String, Int>
        get() = votes.values.groupBy { it }
            .maxByOrNull { it.value.size }
            ?.let { Pair(it.key, it.value.size) }
            ?: Pair("", 0)

    fun addVote(login: String, vote: String): Vote {
        return if (votes.containsKey(login)) {
            votes[login] = vote
            Vote.VOTE_CHANGED
        } else {
            votes[login] = vote
            Vote.NEW_VOTE
        }
    }
}

enum class Vote {
    NEW_VOTE,
    VOTE_CHANGED
}