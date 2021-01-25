package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator

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

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var celebration = SoundConfig("audio/youpi.wav", 1f)
        private var negation = SoundConfig("audio/fail.wav", 1f)
        private var positive = SoundConfig("audio/coin.wav", 1f)
        private var raid = SoundConfig("audio/raid.wav", 1f)
        private val sounds = mutableMapOf<String, SoundConfig>()

        @Dsl
        fun celebration(soundPath: String, gain: Float = 1f) {
            this.celebration = SoundConfig(soundPath, gain)
        }

        @Dsl
        fun negative(soundPath: String, gain: Float = 1f) {
            this.negation = SoundConfig(soundPath, gain)
        }

        @Dsl
        fun positive(soundPath: String, gain: Float = 1f) {
            this.positive = SoundConfig(soundPath, gain)
        }

        @Dsl
        fun raid(soundPath: String, gain: Float = 1f) {
            this.raid = SoundConfig(soundPath, gain)
        }

        @Dsl
        fun custom(id: String, soundPath: String, gain: Float = 1f) {
            sounds[id] = SoundConfig(soundPath, gain)
        }

        fun build(serviceLocator: ServiceLocator): DefaultSoundExtension {
            sounds["celebration"] = celebration
            sounds["negative"] = negation
            sounds["positive"] = positive
            sounds["raid"] = raid

            val logger = serviceLocator.loggerFactory.getLogger(DefaultSoundExtension::class)
            val soundPlayer = SoundPlayer(logger, sounds)
            return DefaultSoundExtension(soundPlayer)
        }
    }

    companion object : TwitckExtension<Configuration, DefaultSoundExtension> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): DefaultSoundExtension {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
        }
    }

}
