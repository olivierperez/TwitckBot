package fr.o80.twitck.extension.sound

import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import java.time.Duration

class SoundCommand(
    private val soundPlayer: SoundPlayer,
    private val extensionProvider: ExtensionProvider
) {

    fun interceptCommandEvent(commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            "!youpi" -> {
                soundPlayer.playYoupi {
                    extensionProvider.showImage("extension-overlay\\src\\main\\resources\\vahine.gif")
                }
            }
            "!yata" -> {
                soundPlayer.playYata {
                    extensionProvider.showImage("extension-overlay\\src\\main\\resources\\vahine.gif")
                }
            }
            "!screen" -> soundPlayer.playScreen()
        }

        return commandEvent
    }

}

private fun ExtensionProvider.showImage(imagePath: String) {
    this.first(OverlayExtension::class).showImage(
        imagePath,
        Duration.ofSeconds(5)
    )
}
