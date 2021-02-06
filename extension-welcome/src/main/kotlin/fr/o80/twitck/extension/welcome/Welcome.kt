package fr.o80.twitck.extension.welcome

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Follower
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.bean.event.JoinEvent
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.bean.event.RaidEvent
import fr.o80.twitck.lib.api.exception.ExtensionDependencyException
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.api.service.time.TimeChecker
import fr.o80.twitck.lib.internal.service.ConfigService
import java.time.Duration

class Welcome(
    private val channel: String,
    private val streamId: String,
    private val ignoredLogins: List<String>,
    private val messagesForFollowers: Collection<String>,
    private val messagesForViewers: Collection<String>,
    private val messagesForBroadcaster: List<String>,
    private val welcomeTimeChecker: TimeChecker,
    private val twitchApi: TwitchApi,
    private val sound: SoundExtension?,
    private val logger: Logger
) {

    private val followers: List<Follower> by lazy {
        twitchApi.getFollowers(streamId)
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
        logger.error("channel => $channel")
        handleNewViewer(raidEvent.channel, raidEvent.viewer, messenger)
        thanksForRaiding(raidEvent, messenger)
        sound?.playRaid()
        return raidEvent
    }

    private fun handleNewViewer(channel: String, viewer: Viewer, messenger: Messenger) {
        if (this.channel != channel)
            return

        if (ignoredLogins.any { viewer.login.equals(it, true) }) {
            return
        }

        if (messagesForBroadcaster.isNotEmpty() && Badge.BROADCASTER in viewer.badges) {
            welcomeTimeChecker.executeIfNotCooldown(viewer.login) {
                val message = messagesForBroadcaster.random()
                    .replace("#USER#", viewer.displayName)
                welcomeHost(channel, message, messenger)
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
            messagesForViewers.random()
                .replace("#USER#", viewer.displayName)
        }
    }

    private fun String.getFollowerOrNull(): Follower? {
        return followers.firstOrNull { follower -> follower.user.name == this }
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Welcome? {
            val config = configService.getConfig("welcome.json", WelcomeConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(Welcome::class)
            logger.info("Installing Welcome extension...")

            val storage = serviceLocator.extensionProvider.firstOrNull(StorageExtension::class)
                ?: throw ExtensionDependencyException("Welcome", "Storage")
            val sound = serviceLocator.extensionProvider.firstOrNull(SoundExtension::class)

            val welcomeTimeChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Welcome::class.java.name,
                flag = "welcomedAt",
                interval = Duration.ofSeconds(config.data.secondsBetweenWelcomes)
            )

            return Welcome(
                channel = config.data.channel,
                streamId = config.data.streamId,
                ignoredLogins = config.data.ignoreViewers,
                messagesForFollowers = config.data.messages.forFollowers,
                messagesForViewers = config.data.messages.forViewers,
                messagesForBroadcaster = config.data.messages.forBroadcaster,
                welcomeTimeChecker = welcomeTimeChecker,
                twitchApi = serviceLocator.twitchApi,
                sound = sound,
                logger = logger
            ).also { welcome ->

                if (config.data.reactTo.commands) {
                    pipeline.interceptCommandEvent { messenger, commandEvent ->
                        welcome.interceptCommandEvent(messenger, commandEvent)
                    }
                }
                if (config.data.reactTo.joins) {
                    pipeline.interceptJoinEvent { messenger, joinEvent ->
                        welcome.interceptJoinEvent(messenger, joinEvent)
                    }
                }
                if (config.data.reactTo.messages) {
                    pipeline.interceptMessageEvent { messenger, messageEvent ->
                        welcome.interceptMessageEvent(messenger, messageEvent)
                    }
                }
                if (config.data.reactTo.raids) {
                    pipeline.interceptRaidEvent { messenger, raid ->
                        welcome.interceptRaidEvent(messenger, raid)
                    }
                }
                pipeline.requestChannel(welcome.channel)
            }
        }
    }

}
