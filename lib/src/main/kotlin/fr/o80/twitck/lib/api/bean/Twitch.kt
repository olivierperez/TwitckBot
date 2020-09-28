package fr.o80.twitck.lib.api.bean

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class Follower(
    val user: User
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "_id")
    val id: String,
    @Json(name = "display_name")
    val displayName: String,
    val name: String,
    val logo: String
)

@JsonClass(generateAdapter = true)
data class Channel(
    @Json(name = "_id")
    val id: String,
    @Json(name = "display_name")
    val displayName: String,
    val game: String?,
    val followers: Int,
    val views: Int,
    val status: String?,
    val url: String,
    val logo: String,
    @Json(name = "video_banner")
    val videoBanner: String
)

@JsonClass(generateAdapter = true)
data class Video(
    @Json(name = "_id")
    val id: String,
    val title: String,
    val description: String?,
    val game: String,
    val url: String,
    @Json(name = "published_at")
    val publishedAt: Date
)

@JsonClass(generateAdapter = true)
data class ValidateResponse(
    @Json(name = "client_id")
    val clientId: String,
    @Json(name = "user_id")
    val userId: String,
    val login: String
)

@JsonClass(generateAdapter = true)
data class NewFollowers(
    val data: List<NewFollower>
)

@JsonClass(generateAdapter = true)
data class NewFollower(
    val from_id: String,
    val from_name: String,
    val to_id: String,
    val to_name: String,
    val followed_at: Date
)