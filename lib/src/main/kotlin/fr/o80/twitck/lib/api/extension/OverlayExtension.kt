package fr.o80.twitck.lib.api.extension

import java.io.InputStream
import java.time.Duration

interface OverlayExtension {
    fun alert(text: String, duration: Duration)
    fun showImage(path: InputStream, duration: Duration)
    fun showImage(path: InputStream, text: String, duration: Duration)
}
