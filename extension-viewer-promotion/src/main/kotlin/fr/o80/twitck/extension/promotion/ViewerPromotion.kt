package fr.o80.twitck.extension.promotion

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.Video
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.api.service.time.TimeChecker
import java.time.Duration
import java.time.Instant

class ViewerPromotion(
    private val channel: String,
    private val promotionMessages: Collection<String>,
    private val ignoredLogins: MutableList<String>,
    private val maxVideoAgeToPromote: Duration,
    private val promotionTimeChecker: TimeChecker,
    private val twitchApi: TwitchApi,
    private val command: ViewerPromotionCommand
) {

    fun interceptMessageEvent(messenger: Messenger, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        if (messageEvent.viewer.login in ignoredLogins) {
            return messageEvent
        }

        promotionTimeChecker.executeIfNotCooldown(messageEvent.viewer.login) {
            promoteViewer(messenger, messageEvent)
        }

        return messageEvent
    }

    private fun promoteViewer(messenger: Messenger, messageEvent: MessageEvent) {
        val lastVideo = twitchApi.getVideos(messageEvent.viewer.userId, 1)
            .filter { (it.publishedAt.toInstant() + maxVideoAgeToPromote).isAfter(Instant.now()) }
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?: return

        val randomMessage = promotionMessages.random().formatViewer(messageEvent, lastVideo)
        messenger.sendWhenAvailable(messageEvent.channel, randomMessage, Importance.HIGH)
    }

    private fun String.formatViewer(messageEvent: MessageEvent, video: Video): String =
        this.replace("#USER#", messageEvent.viewer.displayName)
            .replace("#URL#", video.url)
            .replace("#GAME#", video.game)

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var channel: String? = null

        private val promotionMessages: MutableList<String> = mutableListOf()

        private var ignoredLogins: MutableList<String> = mutableListOf()

        private var intervalBetweenTwoPromotions: Duration = Duration.ofHours(12)

        private var maxVideoAgeToPromote: Duration = Duration.ofDays(120)

        private var messages: ViewerPromotionMessages? = null

        @Dsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @Dsl
        fun addMessage(message: String) {
            promotionMessages += message
        }

        @Dsl
        fun ignore(vararg logins: String) {
            ignoredLogins.addAll(logins)
        }

        @Dsl
        fun promotionInterval(time: Duration) {
            intervalBetweenTwoPromotions = time
        }

        @Dsl
        fun maxVideoAgeToPromote(time: Duration) {
            maxVideoAgeToPromote = time
        }

        @Dsl
        fun messages(
            usage: String,
            noPointsEnough: String,
            shoutOutRecorded: String
        ) {
            messages = ViewerPromotionMessages(
                usage = usage,
                noPointsEnough = noPointsEnough,
                shoutOutRecorded = shoutOutRecorded
            )
        }

        fun build(serviceLocator: ServiceLocator): ViewerPromotion {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${ViewerPromotion::class.simpleName}")

            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)
            val points = serviceLocator.extensionProvider.first(PointsManager::class)

            val promotionTimeChecker = StorageFlagTimeChecker(
                storage,
                ViewerPromotion::class.java.name,
                "promotedAt",
                intervalBetweenTwoPromotions
            )

            val messages = messages ?: throw IllegalStateException("Messages must be defined")

            return ViewerPromotion(
                channel = channelName,
                promotionMessages = promotionMessages,
                ignoredLogins = ignoredLogins,
                maxVideoAgeToPromote = maxVideoAgeToPromote,
                promotionTimeChecker = promotionTimeChecker,
                twitchApi = serviceLocator.twitchApi,
                command = ViewerPromotionCommand(
                    channel = channelName,
                    storage = storage,
                    points = points,
                    messages = messages
                )
            )
        }
    }

    companion object Extension : TwitckExtension<Configuration, ViewerPromotion> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): ViewerPromotion {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
                .also { viewerPromotion ->
                    pipeline.interceptMessageEvent { messenger, messageEvent ->
                        viewerPromotion.interceptMessageEvent(messenger, messageEvent)
                    }
                    pipeline.interceptWhisperCommandEvent { messenger, commandEvent ->
                        viewerPromotion.command.interceptWhisperCommandEvent(
                            messenger,
                            commandEvent
                        )
                    }
                    pipeline.interceptCommandEvent { messenger, commandEvent ->
                        viewerPromotion.command.interceptCommandEvent(messenger, commandEvent)
                    }
                    pipeline.requestChannel(viewerPromotion.channel)
                }
        }
    }
}
