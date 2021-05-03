package fr.o80.twitck.extension.actions.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Status(
    val currentSceneId: String
)
