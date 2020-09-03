package fr.o80.twitck.poll

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.utils.tryToLong
import java.util.Timer
import kotlin.concurrent.schedule

class PollCommands(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val messages: Messages
) {

    private var currentPoll: CurrentPoll? = null

    fun reactTo(bot: TwitckBot, command: Command) {
        when (command.tag) {
            // !poll 120 Est-ce que je dois changer de langage ?
            "!poll" -> handlePoll(bot, command)
            // !vote Non
            "!vote" -> handleVote(command)
        }
    }

    private fun handlePoll(bot: TwitckBot, command: Command) {
        if (command.badges.none { badge -> badge in privilegedBadges }) return

        if (command.options.isNotEmpty()) {
            startPoll(bot, command)
        } else {
            showResult(bot)
        }
    }

    private fun handleVote(command: Command) {
        val vote = command.options.joinToString(" ").toLowerCase()
        currentPoll?.addVote(command.login, vote)
        // TODO OPZ Notifier Points qu'on gagne un certain nombre de points (configurable dans l'extension)
    }

    private fun startPoll(bot: TwitckBot, command: Command) {
        if (command.options.size < 2) {
            bot.send(channel, messages.errorCreationPollUsage)
            return
        }

        // TODO OPZ Voir comment pouvoir définir le sondage en minutes ou heures (1h, 35m ?)
        val seconds = command.options[0].tryToLong()
        val title = command.options.subList(1, command.options.size).joinToString(" ")

        if (seconds == null) {
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

    fun addVote(login: String, vote: String) {
        votes[login] = vote
    }
}