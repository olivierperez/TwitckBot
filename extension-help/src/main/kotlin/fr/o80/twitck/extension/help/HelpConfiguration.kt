package fr.o80.twitck.extension.help

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class HelpConfiguration(
    val channel: String,
    val commands: Map<String, String>
)
