package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.internal.service.CoolDownManager
import java.time.Duration
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl

private const val COOL_DOWN_NAMESPACE = "sound"

class SoundPlayer(
    private val coolDownManager: CoolDownManager
) {

    private val genericCoolDown = CoolDown(Duration.ofSeconds(10))

    fun playYoupi() {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "youpi", genericCoolDown) {
            play("audio/youpi.wav", 6f)
        }
    }

    fun playYata() {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "yata", genericCoolDown) {
            play("audio/yata.wav", 6f)
        }
    }

    fun playScreen() {
        coolDownManager.executeIfCooledDown(COOL_DOWN_NAMESPACE, "screen", genericCoolDown) {
            play("audio/ton_ecran.wav", 6f)
        }
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