package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.extension.Sound
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.service.CoolDownManager
import java.io.BufferedInputStream
import java.time.Duration
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl

private const val COOL_DOWN_NAMESPACE = "sound"

class SoundPlayer(
    private val coolDownManager: CoolDownManager,
    private val logger: Logger
) {

    private val genericCoolDown = CoolDown(Duration.ofSeconds(3))

    fun playCoin(then: () -> Unit = {}) {
        loadAndPlay("audio/coin.wav", 4f)
        then()
    }

    fun playRaid() {
        loadAndPlay("audio/raid.wav", 4f)
    }

    fun playScreen(then: () -> Unit = {}) {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "screen", genericCoolDown) {
            loadAndPlay("audio/ton_ecran.wav", 4f)
            then()
        }
    }

    fun playYoupi(then: () -> Unit = {}) {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "youpi", genericCoolDown) {
            loadAndPlay("audio/youpi.wav", 4f)
            then()
        }
    }

    fun playYata(then: () -> Unit = {}) {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "yata", genericCoolDown) {
            loadAndPlay("audio/yata.wav", 4f)
            then()
        }
    }

    fun play(sound: Sound) {
        loadAndPlay("audio/${sound.file}", 4f)
    }

    private fun loadAndPlay(fileName: String, masterGain: Float) {
        try {
            val clip = AudioSystem.getClip()
            val resourceAsStream = javaClass.classLoader.getResourceAsStream(fileName)
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