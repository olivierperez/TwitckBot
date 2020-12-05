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
                    extensionProvider.showImage("image/vahine.gif", "Youpi !")
                }
            }
            "!yata" -> {
                soundPlayer.playYata {
                    extensionProvider.showImage("image/vahine.gif", "Yata Yata Yata")
                }
            }
            "!screen" -> soundPlayer.playScreen()
        }

        return commandEvent
    }

}

private fun ExtensionProvider.showImage(imagePath: String, text: String) {
    val path = javaClass.classLoader.getResourceAsStream(imagePath)
        ?: throw IllegalArgumentException("Failed to load image for resources: $imagePath")

    this.first(OverlayExtension::class)
        .showImage(path, text, Duration.ofSeconds(5))
}
