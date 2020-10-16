package fr.o80.twitck.extension.welcome

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Follower
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.api.service.time.TimeChecker
import java.time.Duration

class Welcome(
    private val channel: String,
    private val messagesForFollowers: Collection<String>,
    private val messages: Collection<String>,
    private val hostName: String,
    private val hostMessage: String?,
    private val welcomeTimeChecker: TimeChecker,
    private val twitchApi: TwitchApi,
    private val ignoredLogins: MutableList<String>
) {

    private val followers: List<Follower> by lazy {
        val user = twitchApi.getUser(hostName)
        twitchApi.getFollowers(user.id)
    }

    fun interceptJoinEvent(messenger: Messenger, joinEvent: JoinEvent): JoinEvent {
        handleNewViewer(joinEvent.channel, joinEvent.login, messenger)
        return joinEvent
    }

    fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        handleNewViewer(messageEvent.channel, messageEvent.login, messenger)
        return messageEvent
    }

    private fun handleNewViewer(channel: String, login: String, messenger: Messenger) {
        if (this.channel != channel)
            return

        if (ignoredLogins.any { login.equals(it, true) }) {
            return
        }

        if (hostMessage != null && isHost(login)) {
            welcomeHost(messenger, login, hostMessage)
            return
        }

        welcomeTimeChecker.executeIfNotCooldown(login) {
            welcomeViewer(channel, login, messenger)
        }
    }

    private fun isHost(login: String) =
        login == hostName

    private fun welcomeHost(messenger: Messenger, channel: String, message: String) {
        messenger.sendWhenAvailable(channel, message, Importance.LOW)
    }

    private fun welcomeViewer(channel: String, login: String, messenger: Messenger) {
        val randomMessage = pickMessage(login)
        messenger.sendWhenAvailable(channel, randomMessage, Importance.LOW)
    }

    private fun pickMessage(login: String): String {
        val follower = login.getFollowerOrNull()
        return if (follower != null) {
            messagesForFollowers.random().formatFollower(follower)
        } else {
            messages.random().formatViewer(login)
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
        private annotation class Dsl

        private var channel: String? = null

        private val messages: MutableList<String> = mutableListOf()
        private val messagesForFollowers: MutableList<String> = mutableListOf()

        private var hostName: String? = null
        private var hostMessage: String? = null

        private var ignoredLogins: MutableList<String> = mutableListOf()

        private var welcomeInterval: Duration = Duration.ofHours(12)

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun messageForViewer(message: String) {
            messages += message
        }

        @Dsl
        fun messageForFollower(message: String) {
            messagesForFollowers += message
        }

        @Dsl
        fun host(hostName: String, welcomeMessage: String) {
            this.hostName = hostName
            this.hostMessage = welcomeMessage
        }

        @Dsl
        fun ignore(vararg logins: String) {
            ignoredLogins.addAll(logins)
        }

        @Dsl
        fun welcomeInterval(time: Duration) {
            this.welcomeInterval = time
        }

        fun build(serviceLocator: ServiceLocator): Welcome {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Welcome::class.simpleName}")
            val hostName = hostName
                ?: throw IllegalStateException("Host name must be set for the extension ${Welcome::class.simpleName}")

            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val welcomeTimeChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Welcome::class.java.name,
                flag = "welcomedAt",
                interval = welcomeInterval
            )

            return Welcome(
                channel = channelName,
                messagesForFollowers = messagesForFollowers,
                messages = messages,
                hostName = hostName,
                hostMessage = hostMessage,
                welcomeTimeChecker = welcomeTimeChecker,
                twitchApi = serviceLocator.twitchApi,
                ignoredLogins = ignoredLogins
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
                    pipeline.interceptJoinEvent(welcome::interceptJoinEvent)
                    pipeline.interceptMessageEvent(welcome::interceptMessageEvent)
                    pipeline.requestChannel(welcome.channel)
                }
        }
    }
}
