package fr.o80.twitck.poll

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.utils.tryToLong
import java.util.Timer
import kotlin.concurrent.schedule

class PollCommands(
    private val channel: String,
    private val privilegedBadges: Array<out Badge>
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
    }

    private fun startPoll(bot: TwitckBot, command: Command) {
        if (command.options.size < 2) {
            // TODO OPZ Message
            bot.send(channel, "Pour créer un sondage il te faut une durée et un titre !")
            return
        }

        // TODO OPZ Voir comment pouvoir définir le sondage en minutes ou heures (1h, 35m ?)
        val seconds = command.options[0].tryToLong()
        val title = command.options.subList(1, command.options.size).joinToString(" ")

        if (seconds == null) {
            // TODO OPZ Message
            bot.send(channel, "Il faut choisir la durée du sondage !")
            return
        }

        currentPoll = CurrentPoll(title)
        bot.send(channel, "Nouveau sondage : $title")

        Timer().schedule(seconds) {
            currentPoll?.let { poll ->
                bot.send(channel, "Le sondage est fini !! ${poll.result}")
                currentPoll = null
            }
        }
    }

    private fun showResult(bot: TwitckBot) {
        currentPoll?.let { poll ->
            bot.send(channel, "Sondage en cours... ${poll.result}")
        }
    }

}

class CurrentPoll(
    private val title: String
) {
    private val votes: MutableMap<String, String> = mutableMapOf()

    val result: String
        get() = votes.values.groupBy { it }
            .maxByOrNull { it.value.size }
            // TODO OPZ Sortir ça vers la classe PollCommands : message.endedPoll.replace("#TITLE#", title).replace("#BEST#", best.key).replace("#COUNT#", best.value)
            ?.let { "à la question $title Vous avez répondu \"${it.key}\" avec ${it.value.size} résultats" }
            ?: "Pas de résultat"

    fun addVote(login: String, vote: String) {
        votes[login] = vote
    }
}