package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.bean.Follow

interface TwitchApi {
    fun getFollowers(streamId: String): List<Follow>
}
