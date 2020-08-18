package fr.o80.twitck.lib.api.bean

import com.google.gson.annotations.SerializedName

data class Follower(
    val user: User
)

data class User(
    @SerializedName("_id")
    val id: String,
    @SerializedName("display_name")
    val displayName: String,
    val name: String,
    val logo: String
)

data class Channel(
    @SerializedName("_id")
    val id: String,
    @SerializedName("display_name")
    val displayName: String,
    val game: String,
    val followers: Int,
    val views: Int,
    val status: String,
    val url: String,
    val logo: String,
    @SerializedName("video_banner")
    val videoBanner: String
)
