package fr.o80.twitck.extension

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Channel
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.TwitchApi
import java.util.Date
import java.util.concurrent.TimeUnit

class ViewerPromotion(
    private val channel: String,
    private val messages: Collection<String>,
    private val twitchApi: TwitchApi,
    private val ignoredLogins: MutableList<String>
) {
    private val promotedUsers = mutableSetOf<Promoted>()
    private val millisBeforeRePromote = TimeUnit.HOURS.toMillis(1)

    fun interceptMessageEvent(bot: TwitckBot, messageEvent: MessageEvent): MessageEvent {
        if (channel != messageEvent.channel)
            return messageEvent

        if (messageEvent.login in ignoredLogins) {
            return messageEvent
        }

        when {
            needToPromote(messageEvent.login) -> {
                promoteViewer(messageEvent, bot)
            }
            else -> {
                println("> No need to re-promote ${messageEvent.login} yet.") // TODO Utiliser un meilleur système de logging
            }
        }

        return messageEvent
    }

    private fun needToPromote(login: String): Boolean {
        return promotedUsers.firstOrNull { it.login == login }
            ?.let { promotedUser -> promotedUser.date.time + millisBeforeRePromote < System.currentTimeMillis() }
            ?: true
    }

    private fun promoteViewer(messageEvent: MessageEvent, bot: TwitckBot) {
        val viewerChannel = twitchApi.getChannel(messageEvent.userId)
        val randomMessage = messages.random().formatViewer(viewerChannel)
        bot.send(messageEvent.channel, randomMessage)

        promotedUsers.add(Promoted(messageEvent.login))
    }

    private fun String.formatViewer(viewerChannel: Channel): String =
        this.replace("#USER#", viewerChannel.displayName)
            .replace("#URL#", viewerChannel.url)
            .replace("#GAME#", viewerChannel.game)

    class Configuration {

        @DslMarker
        annotation class ViewerPromotionDsl

        private var channel: String? = null

        private val messages: MutableList<String> = mutableListOf()

        private var ignoredLogins: MutableList<String> = mutableListOf()

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

        fun build(serviceLocator: ServiceLocator): ViewerPromotion {
            val channelName = channel
                ?: throw IllegalStateException("Channel must be set for the extension ${ViewerPromotion::class.simpleName}")

            return ViewerPromotion(
                channel = channelName,
                messages = messages,
                twitchApi = serviceLocator.twitchApi,
                ignoredLogins = ignoredLogins
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

private data class Promoted(
    val login: String,
    val date: Date = Date()
)
