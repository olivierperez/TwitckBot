package fr.o80.twitck.extension.repeat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RepeatConfiguration(
    val channel: String,
    val secondsBetweenRepeatedMessages: Long,
    val messages: List<String>,
)
