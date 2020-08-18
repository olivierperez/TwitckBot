package fr.o80.twitck.lib.internal.service

import com.google.gson.GsonBuilder
import fr.o80.twitck.lib.api.bean.Follow
import fr.o80.twitck.lib.api.service.TwitchApi
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TwitchApiImpl(
    private val clientId: String
) : TwitchApi {

    private val gson = GsonBuilder().create()
    private val client = HttpClient.newHttpClient()

    // gnucc -> "124210976"
    override fun getFollowers(streamId: String): List<Follow> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.twitch.tv/kraken/channels/$streamId/follows"))
            .header("Client-ID", clientId)
            //.header("Authorization", "Bearer $oauthToken")
            .header("Accept", "application/vnd.twitchtv.v5+json")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val body = response.body()


        val getFollowAnswer = gson.fromJson(body, FollowAnswer::class.java)
        return getFollowAnswer.follows
    }
}

class FollowAnswer(
    val follows: List<Follow>
)