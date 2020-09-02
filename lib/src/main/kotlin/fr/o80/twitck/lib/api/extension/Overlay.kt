package fr.o80.twitck.lib.api.extension

import java.time.Duration

interface Overlay {
    fun provideInformation(namespace: String, texts: List<String>)
    fun alert(text: String, duration: Duration)
}
