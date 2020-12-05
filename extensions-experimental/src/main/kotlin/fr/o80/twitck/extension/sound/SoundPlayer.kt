package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.bean.CoolDown
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

    private val genericCoolDown = CoolDown(Duration.ofSeconds(10))

    fun playRaid() {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "raid", genericCoolDown) {
            play("audio/raid.wav", 6f)
        }
    }

    fun playScreen(then: () -> Unit = {}) {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "screen", genericCoolDown) {
            play("audio/ton_ecran.wav", 6f)
            then()
        }
    }

    fun playYoupi(then: () -> Unit = {}) {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "youpi", genericCoolDown) {
            play("audio/youpi.wav", 6f)
            then()
        }
    }

    fun playCoin(then: () -> Unit = {}) {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "coin", genericCoolDown) {
            play("audio/coin.wav", 6f)
            then()
        }
    }

    fun playYata(then: () -> Unit = {}) {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "yata", genericCoolDown) {
            play("audio/yata.wav", 6f)
            then()
        }
    }

    private fun play(fileName: String, masterGain: Float) {
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