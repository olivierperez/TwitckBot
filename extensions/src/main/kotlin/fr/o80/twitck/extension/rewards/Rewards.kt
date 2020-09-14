package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelperExtension
import fr.o80.twitck.lib.api.extension.Overlay
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import java.time.Duration

class Rewards(
    private val channel: String,
    private val commands: RewardsCommands,
    private val extensionProvider: ExtensionProvider,
    private val messages: Messages,
    private val talkingTimeChecker: StorageFlagTimeChecker,
    private val rewardedPoints: Int
) {

    private fun onInstallationFinished() {
        extensionProvider.forEach(Overlay::class) { overlay ->
            overlay.provideInformation(
                Rewards::class.java.name,
                listOf("Vous pouvez !claim des ${messages.points} de temps en temps.")
            )
        }
        extensionProvider.forEach(HelperExtension::class) { help ->
            help.registerCommand("!claim")
        }
    }

    fun interceptMessageEvent(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        rewardTalkativeViewers(messageEvent)
        return messageEvent
    }

    private fun rewardTalkativeViewers(messageEvent: MessageEvent) {
        talkingTimeChecker.executeIfNotCooldown(messageEvent.login) {
            extensionProvider.forEach(PointsManager::class) { points ->
                points.addPoints(messageEvent.login, rewardedPoints)
            }
        }
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private var intervalBetweenTwoClaims: Duration = Duration.ofHours(12)
        private var claimedPoints: Int = 10

        private var intervalBetweenTwoTalkRewards: Duration = Duration.ofMinutes(15)
        private var rewardedPoints: Int = 10

        private var messages: Messages? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun claim(points: Int, time: Duration) {
            claimedPoints = points
            intervalBetweenTwoClaims = time
        }

        @Dsl
        fun rewardTalkativeViewers(points: Int, time: Duration) {
            rewardedPoints = points
            intervalBetweenTwoTalkRewards = time
        }

        @Dsl
        fun messages(
            points: String,
            viewerJustClaimed: String,
        ) {
            messages = Messages(
                points,
                viewerJustClaimed
            )
        }

        fun build(serviceLocator: ServiceLocator): Rewards {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Rewards::class.simpleName}")
            val theMessages = messages
                ?: throw IllegalStateException("Messages must be set for the extension ${Rewards::class.simpleName}")

            val lastClaimChecker = StorageFlagTimeChecker(
                storage = serviceLocator.extensionProvider.storage,
                namespace = Rewards::class.java.name,
                flag = "lastClaimedAt",
                interval = intervalBetweenTwoClaims
            )
            val lastTalkChecker = StorageFlagTimeChecker(
                storage = serviceLocator.extensionProvider.storage,
                namespace = Rewards::class.java.name,
                flag = "lastTalkRewardedAt",
                interval = intervalBetweenTwoTalkRewards
            )
            val commands = RewardsCommands(
                channel = channelName,
                extensionProvider = serviceLocator.extensionProvider,
                claimTimeChecker = lastClaimChecker,
                claimedPoints = claimedPoints,
                messages = theMessages
            )
            return Rewards(
                channel = channelName,
                commands = commands,
                extensionProvider = serviceLocator.extensionProvider,
                messages = theMessages,
                talkingTimeChecker = lastTalkChecker,
                rewardedPoints = rewardedPoints
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, Rewards> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Rewards {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { rewards ->
                    pipeline.interceptCommandEvent(rewards.commands::interceptCommandEvent)
                    pipeline.interceptMessageEvent(rewards::interceptMessageEvent)
                    pipeline.requestChannel(rewards.channel)
                    rewards.onInstallationFinished()
                }
        }

    }
}

private val ExtensionProvider.storage: StorageExtension
    get() = provide(StorageExtension::class).first()

