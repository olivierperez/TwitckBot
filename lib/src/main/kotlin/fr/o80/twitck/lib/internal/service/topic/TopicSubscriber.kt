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
import fr.o80.twitck.lib.utils.Do
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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

    // TODO OPZ Changer le secret
    val secret = "TODO Faire générer un secret à la volée"

    override fun run() {
        startWebServer()

        api.validate()

        val callbackUrl = getCallbackUrl()
        subscribeToTopics(callbackUrl)
    }

    private fun subscribeToTopics(callbackUrl: String) {
        // TODO OPZ utiliser le UserName du broadcaster
        val hostUserId = api.getUser("gnu_coding_cafe").id

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

    // TODO OPZ Extraire la partie WebServer dans une classe / package à part
    private fun startWebServer() {
        embeddedServer(Netty, 8080) {
            routing {
                respondTo(Topic.FOLLOWS, ::onNewFollowers)
                respondTo(Topic.STREAMS, ::onStreamChanged)
            }
        }.start(wait = false)
    }

    private fun Routing.respondTo(topic: Topic, block: suspend (ApplicationCall, String) -> Unit) {
        get(topic.path) { respondToChallenge(call) }
        post(topic.path) {
            val body = call.receiveText()
            Do exhaustive when (val result = isSignatureValid(call, body)) {
                SignatureResult.Valid -> {
                    logger.debug("Signature is valid!")
                    block(call, body)
                }
                is SignatureResult.Invalid -> {
                    logger.warn("Failed to check signature \"${result.signature}\" / \"${result.computedSignature}\"! but for now we authorize unchecked signature...\n----\n${result.body}\n----")
                    // TODO OPZ Ne plus laisser passer les appels avec une mauvaise signature (quand on aura vu que ça marche)
                    block(call, body)
                }
                is SignatureResult.Failed -> {
                    logger.error("Something gone wrong while checking signature: ${result.message}")
                    // TODO OPZ Ne plus laisser passer les appels avec une mauvaise signature (quand on aura vu que ça marche)
                    block(call, body)
                }
            }
        }
    }

    private fun isSignatureValid(call: ApplicationCall, body: String): SignatureResult {
        val signature = call.request.headers["X-Hub-Signature"]
            ?: return SignatureResult.Failed("There are no X-Hub-Signature in the headers")

        val computedSignature = body.toSignature(secret)
        if (computedSignature != signature) {
            return SignatureResult.Invalid(signature, computedSignature, body)
        }

        return SignatureResult.Valid
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
            val event = FollowEvent(newFollowers)

            newFollowersHandlers.forEach { handler ->
                logger.debug("New followers, handler: ${handler::class.java}")
                handler(messenger, event)
            }
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

    private fun getCallbackUrl(): String {
        val ngrokTunnel = NgrokTunnel("BotHusky", 8080)
        return ngrokTunnel.getOrOpenTunnel()
    }

    sealed class SignatureResult {
        object Valid : SignatureResult()
        class Invalid(val signature: String, val computedSignature: String, val body: String) : SignatureResult()
        class Failed(val message: String) : SignatureResult()
    }

}

fun String.toSignature(secret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    val signingKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
    mac.init(signingKey)
    val rawSignature = mac.doFinal(this.toByteArray())
    return "sha256=" + rawSignature.fold("") { str, it -> str + "%02x".format(it) }
}
