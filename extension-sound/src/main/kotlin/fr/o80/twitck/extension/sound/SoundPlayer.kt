package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.service.log.Logger
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl

class SoundPlayer(
    private val logger: Logger,
    private val sounds: Map<String, OneSound>
) {

    fun play(id: String) {
        val sound = sounds[id]
        if (sound != null) {
            loadAndPlay(sound.path, sound.gain)
        } else {
            logger.error("Sound \"$id\" is not defined!")
        }
    }

    private fun loadAndPlay(fileName: String, masterGain: Float) {
        try {
            val clip = AudioSystem.getClip()
            val resourceAsStream = javaClass.classLoader.getResourceAsStream(fileName)
                ?: throw IllegalArgumentException("File not found! \"$fileName\"")
            val bufferedAudioStream = BufferedInputStream(resourceAsStream)
            val audioInputStream = AudioSystem.getAudioInputStream(bufferedAudioStream)
            clip.open(audioInputStream)
            val masterGainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            masterGainControl.value = masterGain
            clip.start()
        } catch (e: Exception) {
            logger.error("Something gone wrong while playing $fileName", e)
        }
    }

}