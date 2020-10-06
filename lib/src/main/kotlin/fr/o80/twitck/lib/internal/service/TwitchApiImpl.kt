package fr.o80.twitck.lib.internal.service

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.o80.twitck.lib.api.bean.Channel
import fr.o80.twitck.lib.api.bean.Follower
import fr.o80.twitck.lib.api.bean.User
import fr.o80.twitck.lib.api.bean.ValidateResponse
import fr.o80.twitck.lib.api.bean.Video
import fr.o80.twitck.lib.api.service.TwitchApi
import fr.o80.twitck.lib.api.service.log.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Date

class TwitchApiImpl(
    private val oauthToken: String,
    loggerFactory: LoggerFactory
) : TwitchApi {

    private var clientId: String? = null

    private val logger = loggerFactory.getLogger("NETWORK")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()

    private val client: HttpClient = HttpClient.newHttpClient()

    // TODO OPZ Migrer vers helix => https://dev.twitch.tv/docs/authentication/#sending-user-access-and-app-access-tokens
    private val baseUrl: String = "https://api.twitch.tv/kraken"

    override fun getFollowers(streamId: String): List<Follower> {
        val url = "/channels/$streamId/follows"
        val answer = doRequest(url).parse<FollowAnswer>()
        return answer.follows
    }

    override fun getUser(userName: String): User {
        val url = "/users?login=$userName"
        val answer = doRequest(url).parse<UserAnswer>()
        return answer.users[0]
    }

    override fun getChannel(channelId: String): Channel {
        val url = "/channels/$channelId"
        return doRequest(url).parse()
    }

    override fun getVideos(channelId: String, limit: Int): List<Video> {
        val url = "/channels/$channelId/videos?limit=$limit"
        val videoAnswer = doRequest(url).parse<VideoAnswer>()
        return videoAnswer.videos
    }

    private fun doRequest(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$url"))
            .header("Authorization", "OAuth $oauthToken")
            .header("Accept", "application/vnd.twitchtv.v5+json")
            .apply {
                if (clientId != null) {
                    header("Client-ID", clientId)
                }
            }
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body().also {
            logger.debug("Response: $response")
        }
    }

    override fun subscribeTo(topic: String, callbackUrl: String, leaseSeconds: Long, secret: String): String {
        val clientId = clientId ?: throw IllegalStateException("Client not yet retrieved")

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.twitch.tv/helix/webhooks/hub"))
            .header("Client-ID", clientId)
            .header("Authorization", "Bearer $oauthToken")
            .header("Content-Type", "application/json")
            .POST(buildTopicSubscriptionPayload(callbackUrl, topic, leaseSeconds, "subscribe", secret))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    override fun validate(): ValidateResponse {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://id.twitch.tv/oauth2/validate"))
            .header("Authorization", "Bearer $oauthToken")
            .header("Content-Type", "application/json")
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body().parse<ValidateResponse>().also { validateResponse ->
            clientId = validateResponse.clientId
        }
    }

    private inline fun <reified T> String.parse(): T {
        return moshi.adapter(T::class.java).fromJson(this)!!
    }

    private fun buildTopicSubscriptionPayload(
        callbackUrl: String,
        topic: String,
        leaseSeconds: Long,
        mode: String,
        secret: String
    ): HttpRequest.BodyPublisher {
        return HttpRequest.BodyPublishers.ofString("""
                {
                    "hub.callback": "$callbackUrl",
                    "hub.mode": "$mode",
                    "hub.topic": "$topic",
                    "hub.lease_seconds": $leaseSeconds,
                    "hub.secret": "$secret"
                }
            """.trimIndent())
    }
}

@JsonClass(generateAdapter = true)
class FollowAnswer(
    val follows: List<Follower>
)

@JsonClass(generateAdapter = true)
class UserAnswer(
    val users: List<User>
)

@JsonClass(generateAdapter = true)
class VideoAnswer(
    val videos: List<Video>
)