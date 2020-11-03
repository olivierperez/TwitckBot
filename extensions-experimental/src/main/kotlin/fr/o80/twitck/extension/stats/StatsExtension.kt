package fr.o80.twitck.extension.stats

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.*
import fr.o80.twitck.lib.api.bean.subscription.SubscriptionEvent
import fr.o80.twitck.lib.api.extension.Stat
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator

class StatsExtension(
    private val channel: String,
    private val statsData: StatsData,
    private val statsCommand: StatsCommand
) : Stat {

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        val commandName = commandEvent.command.tag.removePrefix("!")
        statsData.hit(STATS_NAMESPACE, "command", mapOf("command" to commandName))
        return commandEvent
    }

    fun interceptFollowEvent(messenger: Messenger, followsEvent: FollowsEvent): FollowsEvent {
        statsData.hit(STATS_NAMESPACE, "follows")
        return followsEvent
    }

    fun interceptJoinEvent(messenger: Messenger, joinEvent: JoinEvent): JoinEvent {
        statsData.hit(STATS_NAMESPACE, "joins")
        return joinEvent
    }

    fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        statsData.hit(
            STATS_NAMESPACE,
            "bits",
            mapOf(
                STAT_INFO_COUNT to messageEvent.bits,
                STAT_INFO_VIEWER to messageEvent.viewer.login
            )

        )

        val wordsCount = messageEvent.message.split("\\s+".toRegex()).count()
        statsData.hit(
            STATS_NAMESPACE,
            "messages",
            mapOf(
                STAT_INFO_SIZE to messageEvent.message.length,
                STAT_INFO_COUNT to wordsCount
            )
        )

        return messageEvent
    }

    fun interceptSubscriptionEvent(
        messenger: Messenger,
        subscriptionEvent: SubscriptionEvent
    ): SubscriptionEvent {
        statsData.hit(
            STATS_NAMESPACE, "subscriptions"
        )
        return subscriptionEvent
    }

    fun interceptWhisperEvent(messenger: Messenger, whisperEvent: WhisperEvent): WhisperEvent {
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

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        fun build(): StatsExtension {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${StatsExtension::class.simpleName}")

            val statsData = StatsData()

            val statsCommand = StatsCommand(statsData)

            return StatsExtension(channelName, statsData, statsCommand)
        }
    }

    companion object Extension : TwitckExtension<Configuration, StatsExtension> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): StatsExtension {
            return Configuration()
                .apply(configure)
                .build()
                .also { stats ->
                    pipeline.requestChannel(stats.channel)
                    pipeline.interceptCommandEvent(stats::interceptCommandEvent)
                    pipeline.interceptFollowEvent(stats::interceptFollowEvent)
                    pipeline.interceptJoinEvent(stats::interceptJoinEvent)
                    pipeline.interceptMessageEvent(stats::interceptMessageEvent)
                    pipeline.interceptSubscriptionEvent(stats::interceptSubscriptionEvent)
                    pipeline.interceptWhisperEvent(stats::interceptWhisperEvent)
                    pipeline.interceptCommandEvent(stats.statsCommand::interceptCommandEvent)
                }
        }
    }

}