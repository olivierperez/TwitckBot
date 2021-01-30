package fr.o80.twitck.extension.stats

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class StatsConfiguration(
    val channel: String
)