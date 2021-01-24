package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.Sound
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator

class DefaultSoundExtension(
    private val soundCommand: SoundCommand,
    private val soundPlayer: SoundPlayer,
) : SoundExtension {

    override fun playRaid() {
        soundPlayer.playRaid()
    }

    override fun playYata() {
        soundPlayer.playYata()
    }

    override fun playYoupi() {
        soundPlayer.playYoupi()
    }

    override fun playCoin() {
        soundPlayer.playCoin()
    }

    override fun play(sound: Sound) {
        soundPlayer.play(sound)
    }

    class Configuration {

        @DslMarker
        private annotation class Dsl

        fun build(serviceLocator: ServiceLocator): DefaultSoundExtension {
            val logger = serviceLocator.loggerFactory.getLogger(DefaultSoundExtension::class)
            val soundPlayer = SoundPlayer(serviceLocator.coolDownManager, logger)
            val soundCommand = SoundCommand(soundPlayer, serviceLocator.extensionProvider)
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
                    pipeline.interceptCommandEvent { _, commandEvent ->
                        sound.soundCommand.interceptCommandEvent(commandEvent)
                    }
                }
        }
    }

}
