package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.bean.Follower
import fr.o80.twitck.lib.api.bean.User

interface TwitchApi {
    fun getFollowers(streamId: String): List<Follower>
    fun getUser(userName: String): User
}
