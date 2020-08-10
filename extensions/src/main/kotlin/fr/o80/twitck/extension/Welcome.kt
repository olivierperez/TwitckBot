package fr.o80.twitck.extension

import fr.o80.twitck.lib.Pipeline
import fr.o80.twitck.lib.bean.JoinEvent
import fr.o80.twitck.lib.bot.TwitckBot
import fr.o80.twitck.lib.extension.TwitckExtension
import fr.o80.twitck.lib.service.ServiceLocator
import java.util.Date
import java.util.concurrent.TimeUnit

class Welcome(
    private val channel: String,
    private val messages: Collection<String>,
    private val hostName: String?,
    private val hostMessage: String?
) {
    private val welcomedUsers = mutableSetOf<Welcomed>()
    private val millisBeforeReWelcome = TimeUnit.HOURS.toMillis(1)

    fun interceptJoinEvent(bot: TwitckBot, joinEvent: JoinEvent): JoinEvent {
        if (channel != joinEvent.channel)
            return joinEvent

        when {
            isHost(joinEvent.login) -> {
                welcomeHost(bot, joinEvent)
            }
            needToWelcome(joinEvent.login) -> {
                welcomeViewer(joinEvent, bot)
            }
            else -> {
                println("> No need to re-welcome ${joinEvent.login} yet.")
            }
        }

        return joinEvent
    }

    private fun isHost(login: String) =
        login == hostName

    private fun welcomeHost(bot: TwitckBot, joinEvent: JoinEvent) {
        bot.send(joinEvent.channel, hostMessage!!)
    }

    private fun welcomeViewer(joinEvent: JoinEvent, bot: TwitckBot) {
        val randomMessage = formatRandomMessageFor(joinEvent.login)
        bot.send(joinEvent.channel, randomMessage)

        welcomedUsers.add(Welcomed(joinEvent.login))
    }

    private fun needToWelcome(login: String): Boolean {
        return welcomedUsers.firstOrNull { it.login == login }
            ?.let { welcomedUser -> welcomedUser.date.time + millisBeforeReWelcome < System.currentTimeMillis() }
            ?: true
    }

    private fun formatRandomMessageFor(login: String): String =
        messages.random().replace("#USER#", login)

    class Configuration {

        @DslMarker
        annotation class WelcomeDsl

        private var channel: String? = null

        private val messages: MutableList<String> = mutableListOf()

        private var hostName: String? = null
        private var hostMessage: String? = null

        @WelcomeDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @WelcomeDsl
        fun addMessage(message: String) {
            messages += message
        }

        @WelcomeDsl
        fun host(hostName: String, welcomeMessage: String) {
            this.hostName = hostName
            this.hostMessage = welcomeMessage
        }

        fun build(): Welcome {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Welcome::class.simpleName}")

            return Welcome(
                channel = channelName,
                messages = messages,
                hostName = hostName,
                hostMessage = hostMessage
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Welcome> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Welcome {
            return Configuration()
                .apply(configure)
                .build()
                .also { welcome ->
                    pipeline.interceptJoinEvent { bot, joinEvent -> welcome.interceptJoinEvent(bot, joinEvent) }
                    pipeline.requestChannel(welcome.channel)
                }
        }
    }
}

private data class Welcomed(
    val login: String,
    val date: Date = Date()
)
