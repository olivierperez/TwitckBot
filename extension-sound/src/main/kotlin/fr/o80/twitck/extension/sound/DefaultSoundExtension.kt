package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.ConfigService

class DefaultSoundExtension(
    private val soundPlayer: SoundPlayer,
) : SoundExtension {

    override fun play(id: String) {
        soundPlayer.play(id)
    }

    override fun playCelebration() {
        soundPlayer.play("celebration")
    }

    override fun playNegative() {
        soundPlayer.play("negative")
    }

    override fun playPositive() {
        soundPlayer.play("positive")
    }

    override fun playRaid() {
        soundPlayer.play("raid")
    }

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): SoundExtension? {
            val config = configService.getConfig("sound.json", SoundConfiguration::class)
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(DefaultSoundExtension::class)
            logger.info("Installing Sound extension...")

            val sounds = mutableMapOf<String, OneSound>().apply {
                config.data.custom.forEach { (id, oneSound) ->
                    put(id, oneSound)
                }
                put("celebration", config.data.celebration)
                put("negative", config.data.negative)
                put("positive", config.data.positive)
                put("raid", config.data.raid)
            }

            val soundPlayer = SoundPlayer(logger, sounds)

            return DefaultSoundExtension(soundPlayer)
        }
    }

}
