package fr.o80.twitck.lib.api.extension

import java.time.Duration

interface OverlayExtension {
    fun alert(text: String, duration: Duration)
    fun onEvent(event: OverlayEvent)
    fun showImage(path: String, duration: Duration)
    fun showImage(path: String, text: String, duration: Duration)
}

class OverlayEvent(val text: String)
