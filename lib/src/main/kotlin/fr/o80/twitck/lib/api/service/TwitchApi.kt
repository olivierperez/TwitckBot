package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.bean.Channel
import fr.o80.twitck.lib.api.bean.Follower
import fr.o80.twitck.lib.api.bean.User
import fr.o80.twitck.lib.api.bean.ValidateResponse
import fr.o80.twitck.lib.api.bean.Video

interface TwitchApi {
    fun getFollowers(streamId: String): List<Follower>
    fun getUser(userName: String): User
    fun getChannel(channelId: String): Channel
    fun getVideos(channelId: String, limit: Int): List<Video>
    fun subscribeTo(topic: String, callbackUrl: String, leaseSeconds: Long, secret: String): String
    fun unsubscribeFrom(topic: String, callbackUrl: String, leaseSeconds: Long): String
    fun validate(): ValidateResponse
}
