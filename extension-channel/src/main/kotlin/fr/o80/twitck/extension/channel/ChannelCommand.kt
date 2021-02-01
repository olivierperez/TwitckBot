package fr.o80.twitck.extension.channel

import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.Messenger
import java.time.Duration

class ChannelCommands(
    private val commandConfigs: Map<String, List<CommandStep>>,
    private val extensionProvider: ExtensionProvider
) {

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        commandConfigs[commandEvent.command.tag]?.let { commandConfig ->
            executeCommand(commandConfig)
        }

        return commandEvent
    }

    private fun executeCommand(
        steps: List<CommandStep>
    ) {
        steps.forEach { step ->
            when (step) {
                is SoundStep -> play(step)
                is OverlayStep -> show(step)
            }
        }
    }

    private fun play(step: SoundStep) {
        extensionProvider.first(SoundExtension::class)
            .play(step.soundId)
    }

    private fun show(step: OverlayStep) {
        val inputStream = javaClass.classLoader.getResourceAsStream(step.image)
            ?: throw IllegalArgumentException("Failed to load image for resources: ${step.image}")

        extensionProvider.first(OverlayExtension::class)
            .showImage(inputStream, step.text, Duration.ofSeconds(step.seconds))
    }
}
