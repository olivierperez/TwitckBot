package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.internal.service.ConfigService
import java.time.Duration

class Rewards(
    private val channel: String,
    private val commands: RewardsCommands,
    private val extensionProvider: ExtensionProvider,
    private val talkingTimeChecker: StorageFlagTimeChecker,
    private val rewardedPoints: Int
) {

    private fun onInstallationFinished() {
        extensionProvider.forEach(HelpExtension::class) { help ->
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
            extensionProvider.forEach(PointsExtension::class) { points ->
                points.addPoints(messageEvent.viewer.login, rewardedPoints)
            }
        }
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Rewards {
            val config = configService.getConfig("rewards.json", RewardsConfiguration::class)
            val channelName = config.channel

            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val lastClaimChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Rewards::class.java.name,
                flag = "claimedAt",
                interval = Duration.ofSeconds(config.secondsBetweenTwoClaims)
            )
            val lastTalkChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Rewards::class.java.name,
                flag = "talkedRewardedAt",
                interval = Duration.ofSeconds(config.secondsBetweenTwoTalkRewards)
            )

            val commands = RewardsCommands(
                channel = channelName,
                extensionProvider = serviceLocator.extensionProvider,
                claimTimeChecker = lastClaimChecker,
                claimedPoints = config.claimedPoints,
                messages = config.messages
            )

            return Rewards(
                channel = channelName,
                commands = commands,
                extensionProvider = serviceLocator.extensionProvider,
                talkingTimeChecker = lastTalkChecker,
                rewardedPoints = config.rewardedPoints
            ).also { rewards ->
                pipeline.requestChannel(rewards.channel)
                pipeline.interceptCommandEvent { _, commandEvent ->
                    rewards.commands.interceptCommandEvent(commandEvent)
                }
                pipeline.interceptMessageEvent { _, messageEvent ->
                    rewards.interceptMessageEvent(messageEvent)
                }
                rewards.onInstallationFinished()
            }
        }
    }

}

