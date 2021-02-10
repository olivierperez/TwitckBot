package fr.o80.twitck.overlay.model

import java.time.Duration
import java.time.Instant

class LwjglEvent(
    val text: String,
    val since: Instant,
    val duration: Duration
)
