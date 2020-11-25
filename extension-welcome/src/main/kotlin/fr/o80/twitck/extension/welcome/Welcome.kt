package fr.o80.twitck.extension.welcome

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.*
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
    private val ignoredLogins: MutableList<String>,
    private var reactToJoins: Boolean,
    private var reactToMessages: Boolean,
    private var reactToCommands: Boolean,
    private var reactToRaids: Boolean,
    private val welcomeTimeChecker: TimeChecker,
    private val twitchApi: TwitchApi
) {

    private val followers: List<Follower> by lazy {
        val user = twitchApi.getUser(hostName)
        twitchApi.getFollowers(user.id)
    }

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        handleNewViewer(commandEvent.channel, commandEvent.viewer, messenger)
        return commandEvent
    }

    fun interceptJoinEvent(messenger: Messenger, joinEvent: JoinEvent): JoinEvent {
        handleNewViewer(joinEvent.channel, joinEvent.viewer, messenger)
        return joinEvent
    }

    fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        handleNewViewer(messageEvent.channel, messageEvent.viewer, messenger)
        return messageEvent
    }

    fun interceptRaidEvent(messenger: Messenger, raidEvent: RaidEvent): RaidEvent {
        handleNewViewer(raidEvent.channel, raidEvent.viewer, messenger)
        thanksForRaiding(raidEvent, messenger)
        return raidEvent
    }

    private fun handleNewViewer(channel: String, viewer: Viewer, messenger: Messenger) {
        if (this.channel != channel)
            return

        if (ignoredLogins.any { viewer.login.equals(it, true) }) {
            return
        }

        if (hostMessage != null && isHost(viewer.login)) {
            welcomeTimeChecker.executeIfNotCooldown(viewer.login) {
                welcomeHost(channel, hostMessage, messenger)
            }
            return
        }

        welcomeTimeChecker.executeIfNotCooldown(viewer.login) {
            welcomeViewer(channel, viewer, messenger)
        }
    }

    private fun thanksForRaiding(raidEvent: RaidEvent, messenger: Messenger) {
        messenger.sendImmediately(
            raidEvent.channel,
            "Oh ! Merci beaucoup à ${raidEvent.msgDisplayName} et ses ${raidEvent.msgViewerCount} amis pour votre raid !"
        )
    }

    private fun isHost(login: String) =
        login == hostName

    private fun welcomeHost(channel: String, message: String, messenger: Messenger) {
        messenger.sendWhenAvailable(channel, message, Importance.LOW)
    }

    private fun welcomeViewer(channel: String, viewer: Viewer, messenger: Messenger) {
        val randomMessage = pickMessage(viewer)
        messenger.sendWhenAvailable(channel, randomMessage, Importance.LOW)
    }

    private fun pickMessage(viewer: Viewer): String {
        val follower = viewer.login.getFollowerOrNull()
        return if (follower != null) {
            messagesForFollowers.random()
                .replace("#USER#", follower.user.displayName)
        } else {
            messages.random()
                .replace("#USER#", viewer.displayName)
        }
    }

    private fun String.getFollowerOrNull(): Follower? {
        return followers.firstOrNull { follower -> follower.user.name == this }
    }

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

        private var reactToJoins: Boolean = true
        private var reactToMessages: Boolean = true
        private var reactToCommands: Boolean = true
        private var reactToRaids: Boolean = true

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
        fun reactTo(
            joins: Boolean = false,
            messages: Boolean = false,
            commands: Boolean = false,
            raids: Boolean = false
        ) {
            reactToJoins = joins
            reactToMessages = messages
            reactToCommands = commands
            reactToRaids = raids
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
                ignoredLogins = ignoredLogins,
                reactToJoins = reactToJoins,
                reactToMessages = reactToMessages,
                reactToCommands = reactToCommands,
                reactToRaids = reactToRaids,
                welcomeTimeChecker = welcomeTimeChecker,
                twitchApi = serviceLocator.twitchApi
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

                    if (welcome.reactToCommands) {
                        pipeline.interceptCommandEvent { messenger, commandEvent ->
                            welcome.interceptCommandEvent(messenger, commandEvent)
                        }
                    }
                    if (welcome.reactToJoins) {
                        pipeline.interceptJoinEvent { messenger, joinEvent ->
                            welcome.interceptJoinEvent(messenger, joinEvent)
                        }
                    }
                    if (welcome.reactToMessages) {
                        pipeline.interceptMessageEvent { messenger, messageEvent ->
                            welcome.interceptMessageEvent(messenger, messageEvent)
                        }
                    }
                    if (welcome.reactToRaids) {
                        pipeline.interceptRaidEvent { messenger, raid ->
                            welcome.interceptRaidEvent(messenger, raid)
                        }
                    }
                    pipeline.requestChannel(welcome.channel)
                }
        }
    }
}
