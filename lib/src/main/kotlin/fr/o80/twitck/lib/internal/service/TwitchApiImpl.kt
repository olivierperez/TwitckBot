package fr.o80.twitck.lib.internal.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.o80.twitck.lib.api.bean.Channel
import fr.o80.twitck.lib.api.bean.Follower
import fr.o80.twitck.lib.api.bean.User
import fr.o80.twitck.lib.api.bean.Video
import fr.o80.twitck.lib.api.service.TwitchApi
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TwitchApiImpl(
    private val clientId: String,
    private val oauthToken: String
) : TwitchApi {

    private val gson: Gson = GsonBuilder().create()
    private val client: HttpClient = HttpClient.newHttpClient()
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
        return doRequest(url).also { println(">><<\n$it") }.parse()
    }

    override fun getVideos(channelId: String, limit: Int): List<Video> {
        val url = "/channels/$channelId/videos?limit=$limit"
        val videoAnswer = doRequest(url).parse<VideoAnswer>()
        return videoAnswer.videos
    }

    private fun doRequest(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$url"))
            .header("Client-ID", clientId)
            .header("Authorization", "OAuth $oauthToken")
            .header("Accept", "application/vnd.twitchtv.v5+json")
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private inline fun <reified T> String.parse(): T {
        return gson.fromJson(this, T::class.java)
    }
}

class FollowAnswer(
    val follows: List<Follower>
)

class UserAnswer(
    val users: List<User>
)

class VideoAnswer(
    val videos: List<Video>
)