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
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.util.*

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

    private val client: HttpClient = HttpClients.createDefault()

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
        val request = HttpGet("$baseUrl$url").apply {
            addHeader("Authorization", "OAuth $oauthToken")
            addHeader("Accept", "application/vnd.twitchtv.v5+json")
            if (clientId != null) {
                addHeader("Client-ID", clientId)
            }
        }

        val response = client.execute(request)
        val entity = response.entity
        val body = EntityUtils.toString(entity)
        logger.debug("Response: $body")

        return body
    }

    override fun subscribeTo(
        topic: String,
        callbackUrl: String,
        leaseSeconds: Long,
        secret: String
    ): String {
        val clientId = clientId ?: throw IllegalStateException("Client not yet retrieved")

        val request = HttpPost("https://api.twitch.tv/helix/webhooks/hub")
            .apply {
                addHeader("Client-ID", clientId)
                addHeader("Authorization", "Bearer $oauthToken")
                addHeader("Content-Type", "application/json")
                val payload = buildTopicSubscriptionPayload(
                    callbackUrl,
                    topic,
                    leaseSeconds,
                    "subscribe",
                    secret
                )
                entity = StringEntity(payload)
            }

        val response = client.execute(request)
        return EntityUtils.toString(response.entity)
    }

    override fun unsubscribeFrom(topic: String, callbackUrl: String, leaseSeconds: Long): String {
        val clientId = clientId ?: throw IllegalStateException("Client not yet retrieved")

        val request = HttpPost("https://api.twitch.tv/helix/webhooks/hub")
            .apply {
                addHeader("Client-ID", clientId)
                addHeader("Authorization", "Bearer $oauthToken")
                addHeader("Content-Type", "application/json")
                val payload = buildTopicSubscriptionPayload(
                    callbackUrl,
                    topic,
                    leaseSeconds,
                    "unsubscribe",
                    ""
                )
                entity = StringEntity(payload)
            }

        val response = client.execute(request)
        return EntityUtils.toString(response.entity)
    }

    override fun validate(): ValidateResponse {
        val request = HttpGet("https://id.twitch.tv/oauth2/validate")
            .apply {
                addHeader("Authorization", "Bearer $oauthToken")
                addHeader("Content-Type", "application/json")
            }

        val response = client.execute(request)
        val body = EntityUtils.toString(response.entity)
        return body.parse<ValidateResponse>().also { validateResponse ->
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
    ): String {
        return """
                {
                    "hub.callback": "$callbackUrl",
                    "hub.mode": "$mode",
                    "hub.topic": "$topic",
                    "hub.lease_seconds": $leaseSeconds,
                    "hub.secret": "$secret"
                }
            """.trimIndent()
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