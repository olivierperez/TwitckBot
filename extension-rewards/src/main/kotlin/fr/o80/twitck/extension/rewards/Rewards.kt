package fr.o80.twitck.extension.rewards

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.exception.ExtensionDependencyException
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.internal.service.ConfigService
import java.time.Duration

class Rewards(
    private val channel: String,
    private val rewardedPoints: Int,
    private val claimConfig: RewardsClaim,
    private val commands: RewardsCommands,
    private val talkingTimeChecker: StorageFlagTimeChecker,
    private val points: PointsExtension,
    help: HelpExtension?
) {

    init {
        help?.registerCommand(claimConfig.command)
    }

    fun interceptMessageEvent(messageEvent: MessageEvent): MessageEvent {
        rewardTalkativeViewers(messageEvent)
        return messageEvent
    }

    private fun rewardTalkativeViewers(messageEvent: MessageEvent) {
        if (rewardedPoints == 0) return

        talkingTimeChecker.executeIfNotCooldown(messageEvent.viewer.login) {
            points.addPoints(messageEvent.viewer.login, rewardedPoints)
        }
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): Rewards? {
            val config = configService.getConfig("rewards.json", RewardsConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            serviceLocator.loggerFactory.getLogger(Rewards::class)
                .info("Installing Rewards extension...")

            val points = serviceLocator.extensionProvider.firstOrNull(PointsExtension::class)
                ?: throw ExtensionDependencyException("Rewards", "Points")
            val storage = serviceLocator.extensionProvider.firstOrNull(StorageExtension::class)
                ?: throw ExtensionDependencyException("Rewards", "Storage")
            val overlay = serviceLocator.extensionProvider.firstOrNull(OverlayExtension::class)
            val sound = serviceLocator.extensionProvider.firstOrNull(SoundExtension::class)
            val help = serviceLocator.extensionProvider.firstOrNull(HelpExtension::class)

            val lastClaimChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Rewards::class.java.name,
                flag = "claimedAt",
                interval = Duration.ofSeconds(config.data.claim.secondsBetweenTwoClaims)
            )
            val lastTalkChecker = StorageFlagTimeChecker(
                storage = storage,
                namespace = Rewards::class.java.name,
                flag = "talkedRewardedAt",
                interval = Duration.ofSeconds(config.data.talk.secondsBetweenTwoTalkRewards)
            )

            val commands = RewardsCommands(
                channel = config.data.channel,
                claimConfig = config.data.claim,
                i18n = config.data.i18n,
                claimTimeChecker = lastClaimChecker,
                points = points,
                overlay = overlay,
                sound = sound
            )

            return Rewards(
                channel = config.data.channel,
                rewardedPoints = config.data.talk.reward,
                claimConfig = config.data.claim,
                commands = commands,
                talkingTimeChecker = lastTalkChecker,
                help = help,
                points = points
            ).also { rewards ->
                pipeline.requestChannel(rewards.channel)
                pipeline.interceptCommandEvent { _, commandEvent ->
                    rewards.commands.interceptCommandEvent(commandEvent)
                }
                pipeline.interceptMessageEvent { _, messageEvent ->
                    rewards.interceptMessageEvent(messageEvent)
                }
            }
        }
    }

}

