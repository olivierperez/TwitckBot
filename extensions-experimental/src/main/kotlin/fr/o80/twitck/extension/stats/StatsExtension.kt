package fr.o80.twitck.extension.stats

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.FollowsEvent
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.bean.WhisperEvent
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
        statsData.increment("stats", "command", "command" to commandName)
        return commandEvent
    }

    fun interceptFollowEvent(messenger: Messenger, followsEvent: FollowsEvent): FollowsEvent {
        statsData.increment("stats", "follows")
        return followsEvent
    }

    fun interceptJoinEvent(messenger: Messenger, joinEvent: JoinEvent): JoinEvent {
        statsData.increment("stats", "joins")
        return joinEvent
    }

    fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        statsData.increment("stats", "bits", "count" to messageEvent.bits)
        statsData.maximum("stats", "bits", messageEvent.bits)
        statsData.increment("stats", "messages")

        val wordsCount = messageEvent.message.split("\\s+".toRegex()).count()
        statsData.increment("stats", "words", wordsCount)
        statsData.minimum("stats", "words", wordsCount)
        statsData.maximum("stats", "words", wordsCount)
        return messageEvent
    }

    fun interceptSubscriptionEvent(messenger: Messenger, subscriptionEvent: SubscriptionEvent): SubscriptionEvent {
        statsData.increment("stats", "subscriptions")
        return subscriptionEvent
    }

    fun interceptWhisperEvent(messenger: Messenger, whisperEvent: WhisperEvent): WhisperEvent {
        statsData.increment("stats", "whispers")
        return whisperEvent
    }

    override fun increment(namespace: String, key: String, count: Long) {
        if (namespace == "stats") return
        statsData.increment(namespace, key, count)
    }

    override fun maximum(namespace: String, key: String, value: Long) {
        if (namespace == "stats") return
        statsData.maximum(namespace, key, value)
    }

    override fun minimum(namespace: String, key: String, value: Long) {
        if (namespace == "stats") return
        statsData.minimum(namespace, key, value)
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