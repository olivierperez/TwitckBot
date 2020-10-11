package fr.o80.twitck.lib.internal.service.topic

import java.time.Duration

enum class Topic {
    FOLLOWS {
        override val leaseDuration: Duration
            get() = Duration.ofHours(6)

        override val path: String
            get() = "/twitch-follows"

        override fun topicUrl(userId: String): String =
            "https://api.twitch.tv/helix/users/follows?first=1&to_id=$userId"
    },
    STREAMS {
        override val leaseDuration: Duration
            get() = Duration.ofHours(6)

        override val path: String
            get() = "/twitch-streams"

        override fun topicUrl(userId: String): String =
            "https://api.twitch.tv/helix/streams?user_id=$userId"
    };

    abstract val leaseDuration: Duration
    abstract val path: String
    abstract fun topicUrl(userId: String): String

    fun callbackUrl(baseUrl: String): String =
        "$baseUrl$path"
}