package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.ServiceLocator

class Points(
    override val channel: String,
    private val privilegedBadges: Array<out Badge>,
    private val commandParser: CommandParser
) : PointsManager {

    private val bank: MutableMap<String, Int> = mutableMapOf()

    override fun getPoints(login: String): Int {
        return bank.getOrDefault(login, 0)
    }

    override fun addPoints(login: String, points: Int) {
        innerAddPoints(login, points)
    }

    override fun removePoints(login: String, points: Int): Boolean {
        synchronized(bank) {
            return if (canConsume(login, points)) {
                bank.computeIfPresent(login) { _, balance -> balance - points }
                true
            } else {
                false
            }
        }
    }

    private fun interceptMessage(messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        commandParser.parse(messageEvent)?.let { command ->
            reactToCommand(command, messageEvent)
        }

        return messageEvent
    }

    private fun reactToCommand(command: Command, messageEvent: MessageEvent) {
        when (command.tag) {
            // !points_add Pipiks_ 13000
            "!points_add" -> handleAddCommand(command)
            // !points_transfer idontwantgiftsub 152
            "!points_transfer" -> handleTransferCommand(command, messageEvent)
            // TODO !points_info
        }
    }

    private fun handleAddCommand(command: Command) {
        if (command.badges.none { badge -> badge in privilegedBadges }) return

        if (command.options.size == 2) {
            val login = command.options[0].toLowerCase()
            val points = command.options[1].tryToInt()

            points?.let {
                innerAddPoints(login, points)
            }
        }
    }

    private fun handleTransferCommand(command: Command, messageEvent: MessageEvent) {
        if (command.options.size == 2) {
            val login = command.options[0].toLowerCase()
            val points = command.options[1].tryToInt()

            points?.let {
                // TODO Dire si ça a fonctionné, sinon "Les huissiers sont en route"
                innerTransferPoints(messageEvent.login, login, points)
            }
        }
    }

    private fun innerAddPoints(login: String, points: Int) {
        bank.compute(login) { _, balance ->
            (balance ?: 0) + points
        }
    }

    private fun innerTransferPoints(fromLogin: String, toLogin: String, points: Int) {
        synchronized(bank) {
            if (canConsume(fromLogin, points)) {
                bank.computeIfPresent(fromLogin) { _, balance -> balance - points }
                innerAddPoints(toLogin, points)
            }
        }
    }

    private fun canConsume(login: String, points: Int): Boolean {
        return bank.getOrDefault(login, 0) >= points
    }

    private fun String.tryToInt(): Int? {
        return this.takeIf { it.matches("\\d+".toRegex()) }?.toInt()
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null
        private var badges: Array<out Badge>? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun privilegedBadges(vararg badges: Badge) {
            if (badges.isEmpty()) {
                throw IllegalArgumentException("Impossible to set an empty list of privileged badges.")
            }
            this.badges = badges
        }

        fun build(serviceLocator: ServiceLocator): Points {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Points::class.simpleName}")
            val theBadges = badges ?: arrayOf(Badge.BROADCASTER)
            return Points(channelName, theBadges, serviceLocator.commandParser)
        }
    }

    companion object Extension : TwitckExtension<Configuration, Points> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Points {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { points ->
                    pipeline.interceptMessageEvent { _, messageEvent -> points.interceptMessage(messageEvent) }
                    pipeline.requestChannel(points.channel)
                }
        }

    }
}