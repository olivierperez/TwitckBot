package fr.o80.twitck.lib.internal.service.topic

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.o80.twitck.lib.api.bean.FollowEvent
import fr.o80.twitck.lib.api.bean.NewFollowers
import fr.o80.twitck.lib.api.bean.StreamsChanged
import fr.o80.twitck.lib.api.handler.FollowHandler
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.LoggerFactory
import fr.o80.twitck.lib.utils.json.LocalDateTimeAdapter
import io.ktor.application.ApplicationCall
import io.ktor.application.call
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
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TopicSubscriber(
    // private val hostUserId: String,
    private val api: TwitchApi,
    private val messenger: Messenger,
    private val newFollowersHandlers: MutableList<FollowHandler>,
    loggerFactory: LoggerFactory
) : Thread() {

    private val logger = loggerFactory.getLogger(TopicSubscriber::class)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(LocalDateTimeAdapter())
        .build()

    override fun run() {
        startWebServer()

        api.validate()

        val callbackUrl = getCallbackUrl()
        subscribeToTopics(callbackUrl)
    }

    private fun subscribeToTopics(callbackUrl: String) {
        // TODO OPZ utiliser le UserName du broadcaster
        val hostUserId = api.getUser("gnu_coding_cafe").id

        // TODO OPZ Changer le secret
        val secret = "TODO Faire générer un secret à la volée"

        Topic.values().forEach { topic ->
            logger.info("Subscribing to $topic...")
            api.unsubscribeFrom(
                topic = topic.topicUrl(hostUserId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.toSeconds()
            )
            api.subscribeTo(
                topic = topic.topicUrl(hostUserId),
                callbackUrl = topic.callbackUrl(callbackUrl),
                leaseSeconds = topic.leaseDuration.toSeconds(),
                secret = secret
            )
        }
    }

    private fun startWebServer() {
        embeddedServer(Netty, 8080) {
            routing {
                respondTo(Topic.FOLLOWS) { onNewFollowers() }
                respondTo(Topic.STREAMS) { onStreamChanged() }
            }
        }.start(wait = false)
    }

    private fun Routing.respondTo(topic: Topic, block: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit) {
        get(topic.path) { respondToChallenge() }
        post(topic.path, block)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.respondToChallenge() {
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

    private suspend fun PipelineContext<Unit, ApplicationCall>.onNewFollowers() {
        logger.debug("Twitch notified new followers")
        withContext(Dispatchers.IO) {
            val rawResponse = call.receiveText()
            logger.debug("New followers, raw: $rawResponse")
            val newFollowers = moshi.adapter(NewFollowers::class.java).fromJson(rawResponse)!!
            logger.debug("New followers, parsed: $newFollowers")
            val event = FollowEvent(newFollowers)

            newFollowersHandlers.forEach { handler ->
                logger.debug("New followers, handler: ${handler::class.java}")
                handler(messenger, event)
            }
        }
        call.respondText("OK", ContentType.Text.Html)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.onStreamChanged() {
        logger.debug("Twitch notified stream has changed")
        withContext(Dispatchers.IO) {
            val rawResponse = call.receiveText()
            val streamsChanged = moshi.adapter(StreamsChanged::class.java).fromJson(rawResponse)!!

            streamsChanged.data.forEach { streamChanges ->
                logger.info("Stream changes: ${streamChanges.title} - ${streamChanges.type}")
            }
        }
        call.respondText("OK", ContentType.Text.Html)
    }

    private fun getCallbackUrl(): String {
        val ngrokTunnel = NgrokTunnel("BotHusky", 8080)
        return ngrokTunnel.getOrOpenTunnel()
    }

}
