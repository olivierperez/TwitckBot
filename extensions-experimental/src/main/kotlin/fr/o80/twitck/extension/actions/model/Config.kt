package fr.o80.twitck.extension.actions.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    val actions: List<RemoteAction>,
    val scenes: List<Scene>
)
