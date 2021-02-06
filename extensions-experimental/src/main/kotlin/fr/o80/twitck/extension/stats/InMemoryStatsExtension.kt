package fr.o80.twitck.extension.stats

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.bean.event.FollowsEvent
import fr.o80.twitck.lib.api.bean.event.JoinEvent
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.bean.event.WhisperEvent
import fr.o80.twitck.lib.api.bean.subscription.SubscriptionEvent
import fr.o80.twitck.lib.api.extension.StatsExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.internal.service.ConfigService

class InMemoryStatsExtension(
    private val channel: String,
    private val statsData: StatsData,
    private val statsCommand: StatsCommand
) : StatsExtension {

    fun interceptCommandEvent(commandEvent: CommandEvent): CommandEvent {
        statsData.hit(
            STATS_NAMESPACE, "commands", mapOf(
                STAT_INFO_COMMAND to commandEvent.command.tag.removePrefix("!"),
                STAT_INFO_VIEWER to commandEvent.viewer.login
            )
        )
        return commandEvent
    }

    fun interceptFollowEvent(followsEvent: FollowsEvent): FollowsEvent {
        followsEvent.followers.data.forEach { follower ->
            statsData.hit(
                STATS_NAMESPACE,
                "follows",
                mapOf(
                    STAT_INFO_VIEWER to follower.fromName
                )
            )
        }
        return followsEvent
    }

    fun interceptJoinEvent(joinEvent: JoinEvent): JoinEvent {
        statsData.hit(
            STATS_NAMESPACE,
            "joins",
            mapOf(
                STAT_INFO_VIEWER to joinEvent.viewer.login
            )
        )
        return joinEvent
    }

    fun interceptMessageEvent(messageEvent: MessageEvent): MessageEvent {
        statsData.hit(
            STATS_NAMESPACE,
            "bits",
            mapOf(
                STAT_INFO_VIEWER to messageEvent.viewer.login,
                STAT_INFO_COUNT to messageEvent.bits
            )
        )

        statsData.hit(
            STATS_NAMESPACE,
            "messages",
            mapOf(
                STAT_INFO_VIEWER to messageEvent.viewer.login,
                STAT_INFO_SIZE to messageEvent.message.length,
                STAT_INFO_COUNT to messageEvent.message.split("\\s+".toRegex()).count()
            )
        )

        return messageEvent
    }

    fun interceptSubscriptionEvent(
        subscriptionEvent: SubscriptionEvent
    ): SubscriptionEvent {
        statsData.hit(
            STATS_NAMESPACE, "subscriptions"
        )
        return subscriptionEvent
    }

    fun interceptWhisperEvent(whisperEvent: WhisperEvent): WhisperEvent {
        statsData.hit(
            STATS_NAMESPACE, "whispers",
            mapOf(STAT_INFO_COUNT to 1)
        )
        return whisperEvent
    }

    override fun hit(namespace: String, key: String, extra: Map<String, Any>) {
        if (namespace == STATS_NAMESPACE) return
        statsData.hit(namespace, key, extra)
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): StatsExtension? {
            val config = configService.getConfig("stats.json", StatsConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(InMemoryStatsExtension::class)
            logger.info("Installing StatsExtension extension...")

            val channelName = config.data.channel
            val statsData = StatsData()

            val statsCommand = StatsCommand(statsData, logger)

            return InMemoryStatsExtension(channelName, statsData, statsCommand).also { stats ->
                pipeline.requestChannel(stats.channel)
                pipeline.interceptCommandEvent { _, commandEvent ->
                    stats.interceptCommandEvent(commandEvent)
                }
                pipeline.interceptFollowEvent { _, followsEvent ->
                    stats.interceptFollowEvent(followsEvent)
                }
                pipeline.interceptJoinEvent { _, joinEvent ->
                    stats.interceptJoinEvent(joinEvent)
                }
                pipeline.interceptMessageEvent { _, messageEvent ->
                    stats.interceptMessageEvent(messageEvent)
                }
                pipeline.interceptSubscriptionEvent { _, subscriptionEvent ->
                    stats.interceptSubscriptionEvent(subscriptionEvent)
                }
                pipeline.interceptWhisperEvent { _, whisperEvent ->
                    stats.interceptWhisperEvent(whisperEvent)
                }
                pipeline.interceptCommandEvent { messenger, commandEvent ->
                    stats.statsCommand.interceptCommandEvent(messenger, commandEvent)
                }
            }
        }
    }

}