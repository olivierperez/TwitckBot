package fr.o80.twitck.extension.actions.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteAction(
    val name: String,
    val image: String,
    val execute: String
)
