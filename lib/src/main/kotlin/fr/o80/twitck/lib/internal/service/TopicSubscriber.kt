package fr.o80.twitck.lib.internal.service

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
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

class TopicSubscriber(
    private val api: TwitchApi,
    private val messenger: Messenger,
    private val handlers: MutableList<FollowHandler>,
    loggerFactory: LoggerFactory
) : Thread() {

    private val logger = loggerFactory.getLogger(TopicSubscriber::class)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(LocalDateTimeAdapter())
        .build()

    override fun run() {
        startWebServer()
        val url = buildRedirect()
        api.validate()
        // TODO OPZ utiliser le UserName du broadcaster
        val gnuCodingCafeId = api.getUser("gnu_coding_cafe").id
        api.subscribeTo(
            topic = "https://api.twitch.tv/helix/users/follows?first=1&to_id=$gnuCodingCafeId",
            callbackUrl = "$url/twitch-follows",
            leaseSeconds = Duration.ofHours(2).toSeconds()
        )
        api.subscribeTo(
            topic = "https://api.twitch.tv/helix/streams?user_id=$gnuCodingCafeId",
            callbackUrl = "$url/twitch-streams",
            leaseSeconds = Duration.ofMinutes(5).toSeconds()
        )
        logger.info("Subscribed to topics")
    }

    private fun startWebServer() {
        embeddedServer(Netty, 8080) {
            routing {
                get("/twitch-follows") { respondToChallenge("followers") }
                post("/twitch-follows") { onNewFollowers() }
                get("/twitch-streams") { respondToChallenge("streams") }
                post("/twitch-streams") { onStreamChanged() }
            }
        }.start(wait = false)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.respondToChallenge(type: String) {
        val challenge: String = call.parameters["hub.challenge"] ?: "No challenge provided"
        logger.debug("Twitch challenged us for $type with: $challenge")
        call.respondText(challenge, contentType = ContentType.Text.Html)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.onNewFollowers() {
        logger.trace("Twitch notified new followers")
        withContext(Dispatchers.IO) {
            val rawResponse = call.receiveText()
            val newFollowers = moshi.adapter(NewFollowers::class.java).fromJson(rawResponse)!!
            val event = FollowEvent(newFollowers)

            handlers.forEach { handler -> handler(messenger, event) }
        }
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.onStreamChanged() {
        logger.trace("Twitch notified stream has changed")
        withContext(Dispatchers.IO) {
            val rawResponse = call.receiveText()
            val streamsChanged = moshi.adapter(StreamsChanged::class.java).fromJson(rawResponse)!!

            streamsChanged.data.forEach { streamChanges ->
                logger.info("Stream changes: ${streamChanges.title} - ${streamChanges.type}")
            }
        }
    }

    private fun buildRedirect(): String {
        val ngrokTunnel = NgrokTunnel("BotHusky", 8080)
        return ngrokTunnel.url
    }

}
