package fr.o80.twitck.extension.sound

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl

class SoundPlayer {

    fun playYoupi() {
        play("audio/youpi.wav", 6f)
    }

    fun playYata() {
        play("audio/yata.wav", 6f)
    }

    private fun play(fileName: String, masterGain: Float) {
        val clip = AudioSystem.getClip()
        val resourceAsStream = javaClass.classLoader.getResourceAsStream(fileName)
        val audioInputStream = AudioSystem.getAudioInputStream(resourceAsStream)
        clip.open(audioInputStream)
        val masterGainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        masterGainControl.value = masterGain
        clip.start()
    }

}