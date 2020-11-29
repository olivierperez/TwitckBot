package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.*
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import java.time.Duration

class Rewards(
    private val channel: String,
    private val commands: RewardsCommands,
    private val extensionProvider: ExtensionProvider,
    private val talkingTimeChecker: StorageFlagTimeChecker,
    private val rewardedPoints: Int
) {

    private fun onInstallationFinished() {
        extensionProvider.forEach(HelperExtension::class) { help ->
            help.registerCommand("!claim")
        }
    }

    fun interceptMessageEvent(messageEvent: MessageEvent): MessageEvent {
        rewardTalkativeViewers(messageEvent)
        return messageEvent
    }

    private fun rewardTalkativeViewers(messageEvent: MessageEvent) {
        if (rewardedPoints == 0) return

        talkingTimeChecker.executeIfNotCooldown(messageEvent.viewer.login) {
            extensionProvider.forEach(PointsManager::class) { points ->
                points.addPoints(messageEvent.viewer.login, rewardedPoints)
            }
        }
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private var intervalBetweenTwoClaims: Duration = Duration.ofHours(12)
        private var claimedPoints: Int = 0

        private var intervalBetweenTwoTalkRewards: Duration = Duration.ofMinutes(15)
        private var rewardedPoints: Int = 0

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
            viewerJustClaimed: String,
        ) {
            messages = Messages(
                viewerJustClaimed = viewerJustClaimed
            )
        }

        fun build(serviceLocator: ServiceLocator): Rewards {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${Rewards::class.simpleName}")
            val theMessages = messages
                ?: throw IllegalStateException("Messages must be set for the extension ${Rewards::class.simpleName}")

            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val lastClaimChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Rewards::class.java.name,
                flag = "claimedAt",
                interval = intervalBetweenTwoClaims
            )
            val lastTalkChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Rewards::class.java.name,
                flag = "talkedRewardedAt",
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
                    pipeline.interceptCommandEvent { _, commandEvent ->
                        rewards.commands.interceptCommandEvent(commandEvent)
                    }
                    pipeline.interceptMessageEvent { _, messageEvent ->
                        rewards.interceptMessageEvent(messageEvent)
                    }
                    pipeline.requestChannel(rewards.channel)
                    rewards.onInstallationFinished()
                }
        }

    }
}

