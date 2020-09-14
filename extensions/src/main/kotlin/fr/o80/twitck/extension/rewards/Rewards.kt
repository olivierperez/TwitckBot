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
import fr.o80.twitck.lib.utils.tryToLong
import java.util.concurrent.TimeUnit

class Rewards(
    private val channel: String,
    private val commands: RewardsCommands,
    private val extensionProvider: ExtensionProvider,
    private val messages: Messages,
    private val intervalBetweenTwoTalkRewards: Long,
    private val rewardedPoints: Int
) {

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = Rewards::class.java.name

    private fun onInstallationFinished() {
        extensionProvider.forEach(Overlay::class) { overlay ->
            overlay.provideInformation(
                namespace,
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
        if (alreadyRewarded(messageEvent.login)) {
            return
        }

        giveRewardTo(messageEvent.login)
        rememberLastRewardIsNow(messageEvent.login)
    }

    private fun alreadyRewarded(login: String): Boolean {
        val lastTalkRewardedAt = storage.getUserInfo(login, namespace, "lastTalkRewardedAt").tryToLong()
        return lastTalkRewardedAt != null && lastTalkRewardedAt + intervalBetweenTwoTalkRewards > System.currentTimeMillis()
    }

    private fun giveRewardTo(login: String) {
        extensionProvider.forEach(PointsManager::class) { points ->
            points.addPoints(login, rewardedPoints)
        }
    }

    private fun rememberLastRewardIsNow(login: String) {
        storage.putUserInfo(login, namespace, "lastTalkRewardedAt", System.currentTimeMillis().toString())
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private var intervalBetweenTwoClaims: Long = TimeUnit.HOURS.toMillis(12)
        private var claimedPoints: Int = 10

        private var intervalBetweenTwoTalkRewards: Long = TimeUnit.MINUTES.toMillis(15)
        private var rewardedPoints: Int = 10

        private var messages: Messages? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun claim(points: Int, time: Long, unit: TimeUnit) {
            claimedPoints = points
            intervalBetweenTwoClaims = unit.toMillis(time)
        }

        @Dsl
        fun rewardTalkativeViewers(points: Int, time: Long, unit: TimeUnit) {
            rewardedPoints = points
            intervalBetweenTwoTalkRewards = unit.toMillis(time)
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

            val commands = RewardsCommands(
                channel = channelName,
                extensionProvider = serviceLocator.extensionProvider,
                intervalBetweenTwoClaims = intervalBetweenTwoClaims,
                claimedPoints = claimedPoints,
                messages = theMessages
            )
            return Rewards(
                channel = channelName,
                commands = commands,
                extensionProvider = serviceLocator.extensionProvider,
                messages = theMessages,
                intervalBetweenTwoTalkRewards = intervalBetweenTwoTalkRewards,
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
