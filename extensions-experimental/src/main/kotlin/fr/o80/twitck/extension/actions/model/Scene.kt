package fr.o80.twitck.extension.actions.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Scene(
    val id: String,
    val name: String
)
