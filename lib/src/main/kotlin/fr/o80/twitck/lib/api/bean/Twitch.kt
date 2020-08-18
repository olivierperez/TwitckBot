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
