package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Follower
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.utils.tryToLong
import java.util.concurrent.TimeUnit

class Welcome(
    private val channel: String,
    private val messagesForFollowers: Collection<String>,
    private val messages: Collection<String>,
    private val hostName: String,
    private val hostMessage: String?,
    private val welcomeInterval: Long,
    private val twitchApi: TwitchApi,
    private val ignoredLogins: MutableList<String>,
    private val extensionProvider: ExtensionProvider,
    private val logger: Logger
) {

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = Welcome::class.java.name

    private val followers : List<Follower> by lazy {
        val user = twitchApi.getUser(hostName)
        twitchApi.getFollowers(user.id)
    }

    fun interceptJoinEvent(bot: TwitckBot, joinEvent: JoinEvent): JoinEvent {
        if (channel != joinEvent.channel)
            return joinEvent

        if (joinEvent.login in ignoredLogins) {
            return joinEvent
        }

        when {
            isHost(joinEvent.login) -> {
                welcomeHost(bot, joinEvent)
            }
            needToWelcome(joinEvent.login) -> {
                welcomeViewer(joinEvent, bot)
            }
            else -> {
                logger.debug("No need to re-welcome ${joinEvent.login} yet.")
            }
        }

        return joinEvent
    }

    private fun isHost(login: String) =
        login == hostName

    private fun welcomeHost(bot: TwitckBot, joinEvent: JoinEvent) {
        bot.send(joinEvent.channel, hostMessage!!)

        storage.putUserInfo(joinEvent.login, namespace, "lastWelcomeAt", System.currentTimeMillis().toString())
    }

    private fun needToWelcome(login: String): Boolean {
        val lastWelcomeAt = storage.getUserInfo(login, namespace, "lastWelcomeAt").tryToLong() ?: 0
        return lastWelcomeAt + welcomeInterval < System.currentTimeMillis()
    }

    private fun welcomeViewer(joinEvent: JoinEvent, bot: TwitckBot) {
        val randomMessage = pickMessage(joinEvent)
        bot.send(joinEvent.channel, randomMessage)

        storage.putUserInfo(joinEvent.login, namespace, "lastWelcomeAt", System.currentTimeMillis().toString())
    }

    private fun pickMessage(joinEvent: JoinEvent): String {
        val follower = joinEvent.login.getFollowerOrNull()
        return if (follower != null) {
            messagesForFollowers.random().formatFollower(follower)
        } else {
            messages.random().formatViewer(joinEvent.login)
        }
    }

    private fun String.getFollowerOrNull(): Follower? {
        return followers.firstOrNull { follower -> follower.user.name == this }
    }

    private fun String.formatViewer(login: String): String =
        this.replace("#USER#", login)

    private fun String.formatFollower(follower: Follower): String =
        this.replace("#USER#", follower.user.displayName)

    class Configuration {

        @DslMarker
        private annotation class WelcomeDsl

        private var channel: String? = null

        private val messages: MutableList<String> = mutableListOf()
        private val messagesForFollowers: MutableList<String> = mutableListOf()

        private var hostName: String? = null
        private var hostMessage: String? = null

        private var ignoredLogins: MutableList<String> = mutableListOf()

        private var welcomeInterval = TimeUnit.HOURS.toMillis(12)

        @WelcomeDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @WelcomeDsl
        fun messageForViewer(message: String) {
            messages += message
        }

        @WelcomeDsl
        fun messageForFollower(message: String) {
            messagesForFollowers += message
        }

        @WelcomeDsl
        fun host(hostName: String, welcomeMessage: String) {
            this.hostName = hostName
            this.hostMessage = welcomeMessage
        }

        @WelcomeDsl
        fun ignore(vararg logins: String) {
            ignoredLogins.addAll(logins)
        }

        @WelcomeDsl
        fun welcomeInterval(time: Long, unit: TimeUnit) {
            this.welcomeInterval = unit.toMillis(time)
        }

        fun build(serviceLocator: ServiceLocator): Welcome {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Welcome::class.simpleName}")
            val hostName = hostName
                ?: throw IllegalStateException("Host name must be set for the extension ${Welcome::class.simpleName}")

            return Welcome(
                channel = channelName,
                messages = messages,
                messagesForFollowers = messagesForFollowers,
                hostName = hostName,
                hostMessage = hostMessage,
                welcomeInterval = welcomeInterval,
                twitchApi = serviceLocator.twitchApi,
                ignoredLogins = ignoredLogins,
                logger = serviceLocator.loggerFactory.getLogger(Welcome::class),
                extensionProvider = serviceLocator.extensionProvider
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
                .build(serviceLocator)
                .also { welcome ->
                    pipeline.interceptJoinEvent { bot, joinEvent -> welcome.interceptJoinEvent(bot, joinEvent) }
                    pipeline.requestChannel(welcome.channel)
                }
        }
    }
}
