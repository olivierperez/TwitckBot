package fr.o80.twitck.extension.promotion

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.bean.*
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.time.StorageFlagTimeChecker
import fr.o80.twitck.lib.api.service.time.TimeChecker
import fr.o80.twitck.lib.utils.skip
import java.time.Duration

class ViewerPromotion(
    private val channel: String,
    private val messages: Collection<String>,
    private val ignoredLogins: MutableList<String>,
    private val promotionTimeChecker: TimeChecker,
    private val twitchApi: TwitchApi,
    private val commandParser: CommandParser,
    private val storage: StorageExtension
) {

    private val namespace: String = ViewerPromotion::class.java.name

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

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!shoutout" -> handleShoutOut(messenger, commandEvent)
        }
        return commandEvent
    }

    private fun handleShoutOut(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.size < 2) {
            messenger.sendImmediately(commandEvent.channel, "usage: !shoutout <login> <message>")
            return
        }
        val login = commandEvent.command.options[0]
        val message = commandEvent.command.options.skip(1).joinToString(" ")

        if (storage.hasUserInfo(login)) {
            storage.putUserInfo(login, namespace, "!shoutout", message)
        }
    }

    fun interceptWhisperEvent(messenger: Messenger, whisperEvent: WhisperEvent) {
        commandParser.parse(whisperEvent.message)?.let { command ->
            if (command.tag != "!shoutout") return

            if (command.options.size != 1) {
                messenger.sendImmediately(channel, "usage: !shoutout <login>")
                return
            }

            val login = command.options[0]
            if (storage.hasUserInfo(login)) {
                storage.getUserInfo(login, namespace, "!shoutout")?.let {message ->
                    messenger.sendImmediately(
                        channel,
                        message,
                        CoolDown(Duration.ofSeconds(10))
                    )
                }
            }
        }
    }

    private fun promoteViewer(messenger: Messenger, messageEvent: MessageEvent) {
        val lastVideo = twitchApi.getVideos(messageEvent.viewer.userId, 1)
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?: return

        val randomMessage = messages.random().formatViewer(messageEvent, lastVideo)
        messenger.sendWhenAvailable(messageEvent.channel, randomMessage, Importance.HIGH)
    }

    private fun String.formatViewer(messageEvent: MessageEvent, video: Video): String =
        this.replace("#USER#", messageEvent.viewer.displayName)
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

            val storage = serviceLocator.extensionProvider.first(StorageExtension::class)

            val promotionTimeChecker = StorageFlagTimeChecker(
                storage,
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
                commandParser = serviceLocator.commandParser,
                storage = storage
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
                    pipeline.interceptWhisperEvent { messenger, whisperEvent ->
                        viewerPromotion.interceptWhisperEvent(messenger, whisperEvent)
                    }
                    pipeline.interceptCommandEvent { messenger, commandEvent ->
                        viewerPromotion.interceptCommandEvent(messenger, commandEvent)
                    }
                    pipeline.requestChannel(viewerPromotion.channel)
                }
        }
    }
}
