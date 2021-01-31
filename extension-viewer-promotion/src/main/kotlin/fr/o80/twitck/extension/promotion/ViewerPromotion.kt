package fr.o80.twitck.extension.promotion

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.Video
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.HelpExtension
import fr.o80.twitck.lib.api.extension.PointsExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.api.service.time.TimeChecker
import fr.o80.twitck.lib.internal.service.ConfigService
import java.time.Duration
import java.time.Instant

class ViewerPromotion(
    private val channel: String,
    private val promotionMessages: Collection<String>,
    private val ignoredLogins: Collection<String>,
    private val maxVideoAgeToPromote: Duration,
    private val promotionTimeChecker: TimeChecker,
    private val twitchApi: TwitchApi,
    private val command: ViewerPromotionCommand,
    private val extensionProvider: ExtensionProvider
) {

    init {
        extensionProvider.forEach(HelpExtension::class) { help ->
            help.registerCommand(SHOUT_OUT_COMMAND)
        }
    }

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

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): ViewerPromotion {
            val config = configService.getConfig("promotion.json", ViewerPromotionConfiguration::class)

            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)
            val points = serviceLocator.extensionProvider.first(PointsExtension::class)
            val sound = serviceLocator.extensionProvider.first(SoundExtension::class)

            val promotionTimeChecker = StorageFlagTimeChecker(
                storage,
                ViewerPromotion::class.java.name,
                "promotedAt",
                Duration.ofSeconds(config.secondsBetweenTwoPromotions)
            )

            return ViewerPromotion(
                channel = config.channel,
                promotionMessages = config.promotionMessages,
                ignoredLogins = config.ignoreViewers,
                maxVideoAgeToPromote = Duration.ofDays(config.daysSinceLastVideoToPromote),
                promotionTimeChecker = promotionTimeChecker,
                twitchApi = serviceLocator.twitchApi,
                command = ViewerPromotionCommand(
                    channel = config.channel,
                    storage = storage,
                    sound = sound,
                    points = points,
                    messages = config.messages
                ),
                extensionProvider = serviceLocator.extensionProvider
            ).also { viewerPromotion ->
                pipeline.requestChannel(viewerPromotion.channel)
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
            }
        }
    }

}
