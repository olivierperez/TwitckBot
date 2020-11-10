package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator

class DefaultSoundExtension(
    private val soundCommand: SoundCommand,
    private val soundPlayer: SoundPlayer,
): SoundExtension {

    override fun playYata() {
        soundPlayer.playYata()
    }

    override fun playYoupi() {
        soundPlayer.playYoupi()
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        fun build(serviceLocator: ServiceLocator): DefaultSoundExtension {
            val soundPlayer = SoundPlayer(serviceLocator.coolDownManager)
            val soundCommand = SoundCommand(soundPlayer)
            return DefaultSoundExtension(soundCommand, soundPlayer)
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
                .also { sound ->
                    pipeline.interceptCommandEvent(sound.soundCommand::interceptCommandEvent)
                }
        }
    }

}
