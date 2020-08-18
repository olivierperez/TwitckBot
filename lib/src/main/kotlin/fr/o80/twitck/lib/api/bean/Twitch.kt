package fr.o80.twitck.lib.api.bean

import com.google.gson.annotations.SerializedName

data class Follow(
    val user: User
)

data class User(
    @SerializedName("display_name")
    val displayName: String,
    val name: String
)
