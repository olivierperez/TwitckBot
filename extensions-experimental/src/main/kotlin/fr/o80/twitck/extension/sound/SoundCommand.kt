package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.bean.event.CommandEvent

class SoundCommand(
    private val soundPlayer: SoundPlayer
) {

    fun interceptCommandEvent(commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!youpi" -> soundPlayer.playYoupi()
            "!yata" -> soundPlayer.playYata()
            "!screen" -> soundPlayer.playScreen()
        }

        return commandEvent
    }

}
