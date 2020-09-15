package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.bean.Video
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.api.service.time.TimeChecker
import java.time.Duration

class ViewerPromotion(
    private val channel: String,
    private val messages: Collection<String>,
    private val ignoredLogins: MutableList<String>,
    private val promotionTimeChecker: TimeChecker,
    private val twitchApi: TwitchApi,
    private val extensionProvider: ExtensionProvider
) {

    private val storage: StorageExtension by lazy {
        extensionProvider.provide(StorageExtension::class).first()
    }

    private val namespace: String = ViewerPromotion::class.java.name

    fun interceptMessageEvent(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        if (messageEvent.login in ignoredLogins) {
            return messageEvent
        }

        promotionTimeChecker.executeIfNotCooldown(messageEvent.login) {
            promoteViewer(messageEvent, bot)
        }

        return messageEvent
    }

    private fun promoteViewer(messageEvent: MessageEvent, bot: TwitckBot) {
        val lastVideo = twitchApi.getVideos(messageEvent.userId, 1)
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?: return

        val randomMessage = messages.random().formatViewer(messageEvent, lastVideo)
        bot.send(messageEvent.channel, randomMessage)

        storage.putUserInfo(messageEvent.login, namespace, "lastPromotionAt", System.currentTimeMillis().toString())
    }

    private fun String.formatViewer(messageEvent: MessageEvent, video: Video): String =
        this.replace("#USER#", messageEvent.login)
            .replace("#URL#", video.url)
            .replace("#GAME#", video.game)

    class Configuration {

        @DslMarker
        private annotation class ViewerPromotionDsl

        private var channel: String? = null

        private val messages: MutableList<String> = mutableListOf()

        private var ignoredLogins: MutableList<String> = mutableListOf()

        private var intervalBetweenTwoPromotions: Duration = Duration.ofHours(12)

        @ViewerPromotionDsl
        fun channel(channel: String) {
            this.channel = channel
        }

        @ViewerPromotionDsl
        fun addMessage(message: String) {
            messages += message
        }

        @ViewerPromotionDsl
        fun ignore(vararg logins: String) {
            ignoredLogins.addAll(logins)
        }

        @ViewerPromotionDsl
        fun promotionInterval(time: Duration) {
            intervalBetweenTwoPromotions = time
        }

        fun build(serviceLocator: ServiceLocator): ViewerPromotion {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${ViewerPromotion::class.simpleName}")

            val promotionTimeChecker = StorageFlagTimeChecker(
                serviceLocator.extensionProvider.storage,
                ViewerPromotion::class.java.name,
                "promotedAt",
                intervalBetweenTwoPromotions
            )

            return ViewerPromotion(
                channel = channelName,
                messages = messages,
                ignoredLogins = ignoredLogins,
                promotionTimeChecker = promotionTimeChecker,
                twitchApi = serviceLocator.twitchApi,
                extensionProvider = serviceLocator.extensionProvider
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
                    pipeline.interceptMessageEvent { bot, messageEvent -> viewerPromotion.interceptMessageEvent(bot, messageEvent) }
                    pipeline.requestChannel(viewerPromotion.channel)
                }
        }
    }
}

// TODO OPZ ExtensionProvider.first(clazz)
private val ExtensionProvider.storage: StorageExtension
    get() = provide(StorageExtension::class).first()
