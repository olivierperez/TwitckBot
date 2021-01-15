package fr.o80.twitck.extension.actions

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteAction(
    val name: String,
    val image: String,
    val command: String
)
