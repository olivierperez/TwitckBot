package fr.o80.twitck.lib.api.extension

interface SoundExtension {
    fun playCoin()
    fun playRaid()
    fun playYata()
    fun playYoupi()
    fun play(sound: Sound)
}

enum class Sound(val file: String) {
    FAIL("fail.wav")
}
