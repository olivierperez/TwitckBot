package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.NewFollowers
import fr.o80.twitck.lib.api.service.TwitchApi
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import java.time.Duration

class TopicSubscriber(
    private val api: TwitchApi
) : Thread() {

    override fun run() {
        startWebServer()
        val url = buildRedirect()
        api.validate()
        val gnuCodingCafeId = api.getUser("gnu_coding_cafe").id
        api.subscribeTo(
            topic = "https://api.twitch.tv/helix/users/follows?first=1&to_id=$gnuCodingCafeId",
            callbackUrl = "$url/twitch-follows",
            leaseSeconds = Duration.ofHours(2).toSeconds()
        )
        println("Subscribed to topics")
    }

    private fun startWebServer() {
        embeddedServer(Netty, 8080) {
            routing {
                get("/twitch-follows") {
                    respondToChallenge()
                }

                post("/twitch-follows") {
                    onNewFollowers()
                }
            }
        }.start(wait = false)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.respondToChallenge() {
        val challenge = call.parameters["hub.challenge"]
        println("Twitch challenged us with: $challenge")
        call.respondText(challenge ?: "oops", contentType = ContentType.Text.Plain)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.onNewFollowers() {
        println("Twitch notified us en POST")
        val newFollowers = call.receive<NewFollowers>()
        val newFollowersNames = newFollowers.data.joinToString(",") { follower -> follower.from_name }
        println("New followers: $newFollowersNames")
    }

    private fun buildRedirect(): String {
        val ngrokTunnel = NgrokTunnel("BotHusky", 8080)
        return ngrokTunnel.url
    }

}
