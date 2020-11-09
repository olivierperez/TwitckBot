package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.service.Messenger

class SoundCommand(
    private val soundPlayer: SoundPlayer
) {

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!youpi" -> soundPlayer.playYoupi()
            "!yata" -> soundPlayer.playYata()
            "!screen" -> soundPlayer.playScreen()
        }

        return commandEvent
    }

}
