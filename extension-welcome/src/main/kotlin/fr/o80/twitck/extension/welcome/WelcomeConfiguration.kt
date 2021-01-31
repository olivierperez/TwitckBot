package fr.o80.twitck.extension.welcome

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WelcomeConfiguration(
    val streamId: String,
    val channel: String,
    val secondsBetweenWelcomes: Long,
    val ignoreViewers: List<String>,
    val messages: WelcomeMessages,
    val reactTo: WelcomeReactTo
)

@JsonClass(generateAdapter = true)
class WelcomeMessages(
    val forBroadcaster: List<String>,
    val forViewers: List<String>,
    val forFollowers: List<String>
)

@JsonClass(generateAdapter = true)
class WelcomeReactTo(
    val joins: Boolean,
    val messages: Boolean,
    val commands: Boolean,
    val raids: Boolean,
)
