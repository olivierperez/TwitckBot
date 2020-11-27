package fr.o80.twitck.lib.internal.service.topic

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.o80.twitck.lib.api.bean.event.FollowsEvent
import fr.o80.twitck.lib.api.bean.NewFollowers
import fr.o80.twitck.lib.api.bean.StreamsChanged
import fr.o80.twitck.lib.api.bean.subscription.NewSubscription
import fr.o80.twitck.lib.api.bean.subscription.Notification
import fr.o80.twitck.lib.api.bean.subscription.SubscriptionEvent
import fr.o80.twitck.lib.api.bean.subscription.UnknownType
import fr.o80.twitck.lib.api.bean.twitch.TwitchSubscriptionEvents
import fr.o80.twitck.lib.api.service.log.LoggerFactory
import fr.o80.twitck.lib.internal.handler.FollowsDispatcher
import fr.o80.twitck.lib.internal.handler.SubscriptionsDispatcher
import fr.o80.twitck.lib.utils.json.LocalDateTimeAdapter
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DoubleReceive
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebhooksServer(
    private val followsDispatcher: FollowsDispatcher,
    private val subscriptionsDispatcher: SubscriptionsDispatcher,
    private val secret: String,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.getLogger(WebhooksServer::class)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(LocalDateTimeAdapter())
        .build()

    fun start() {
        embeddedServer(Netty, 8080) {
            install(DoubleReceive)
            routing {
                respondTo(Topic.FOLLOWS, ::onNewFollowers)
                respondTo(Topic.SUBSCRIBE, ::onNewSubscription)
                respondTo(Topic.STREAMS, ::onStreamChanged)
            }
        }.start(wait = false)
    }

    private fun Routing.respondTo(topic: Topic, block: suspend (ApplicationCall, String) -> Unit) {
        get(topic.path) { respondToChallenge(call) }
        protectedBySignature(logger, secret) {
            post(topic.path) {
                val body = call.receiveText()
                block(call, body)
            }
        }
    }

    private suspend fun respondToChallenge(call: ApplicationCall) {
        val challenge = call.parameters["hub.challenge"]
        val mode = call.parameters["hub.mode"]

        if (challenge == null) {
            val reason: String = call.parameters["hub.reason"] ?: "No reason given"
            logger.debug("Twitch denied us on ${call.request.path()} because: $reason")
            call.respondText("", contentType = ContentType.Text.Html)
        } else {
            logger.debug("Twitch challenged us for $mode on ${call.request.path()} with $challenge")
            call.respondText(challenge, contentType = ContentType.Text.Html)
        }
    }

    private suspend fun onNewFollowers(call: ApplicationCall, body: String) {
        logger.debug("Twitch notified new followers")
        withContext(Dispatchers.IO) {
            val newFollowers = moshi.adapter(NewFollowers::class.java).fromJson(body)!!
            logger.debug("New followers, parsed: $newFollowers")

            val event = FollowsEvent(newFollowers)
            followsDispatcher.dispatch(event)
        }
        call.respondText("OK", ContentType.Text.Html)
    }

    private suspend fun onNewSubscription(call: ApplicationCall, body: String) {
        logger.debug("Twitch notified subscriptions")
        withContext(Dispatchers.IO) {
            val subscriptionEvents = moshi.adapter(TwitchSubscriptionEvents::class.java).fromJson(body)!!
            logger.debug("Subscription events, parsed: $subscriptionEvents")

            val eventsByType = subscriptionEvents.data.groupBy { it.type }
            eventsByType["subscriptions.subscribe"]
                ?.let { events -> SubscriptionEvent(NewSubscription, events.map { event -> event.data }) }
                ?.also { subscriptionsDispatcher.dispatch(it) }
            eventsByType["subscriptions.notification"]
                ?.let { events -> SubscriptionEvent(Notification, events.map { event -> event.data }) }
                ?.also { subscriptionsDispatcher.dispatch(it) }

            eventsByType.entries
                .asSequence()
                .filterNot { (eventType, _) ->
                    eventType in listOf("subscriptions.subscribe", "subscriptions.notification")
                }
                .map { (eventType, events) ->
                    SubscriptionEvent(UnknownType(eventType), events.map { event -> event.data })
                }
                .forEach { subscriptionsDispatcher.dispatch(it) }
        }
        call.respondText("OK", ContentType.Text.Html)
    }

    private suspend fun onStreamChanged(call: ApplicationCall, body: String) {
        logger.debug("Twitch notified stream has changed")
        withContext(Dispatchers.IO) {
            val streamsChanged = moshi.adapter(StreamsChanged::class.java).fromJson(body)!!

            streamsChanged.data.forEach { streamChanges ->
                logger.info("Stream changes: ${streamChanges.title} - ${streamChanges.type}")
            }
        }
        call.respondText("OK", ContentType.Text.Html)
    }
}
