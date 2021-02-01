package fr.o80.twitck.extension.channel

import fr.o80.twitck.extension.channel.config.CommandStep
import fr.o80.twitck.extension.channel.config.MessageStep
import fr.o80.twitck.extension.channel.config.OverlayStep
import fr.o80.twitck.extension.channel.config.SoundStep
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.Messenger
import java.time.Duration

class StepsExecutor(
    private val extensionProvider: ExtensionProvider
) {
    fun execute(
        steps: List<CommandStep>,
        messenger: Messenger,
        commandEvent: CommandEvent
    ) {
        steps.forEach { step ->
            when (step) {
                is MessageStep -> send(step, messenger, commandEvent)
                is OverlayStep -> show(step)
                is SoundStep -> play(step)
            }
        }
    }

    private fun send(step: MessageStep, messenger: Messenger, commandEvent: CommandEvent) {
        val message = step.message.formatFor(commandEvent.viewer)
        messenger.sendImmediately(commandEvent.channel, message)
    }

    private fun show(step: OverlayStep) {
        val inputStream = javaClass.classLoader.getResourceAsStream(step.image)
            ?: throw IllegalArgumentException("Failed to load image for resources: ${step.image}")

        extensionProvider.first(OverlayExtension::class)
            .showImage(inputStream, step.text, Duration.ofSeconds(step.seconds))
    }

    private fun play(step: SoundStep) {
        extensionProvider.first(SoundExtension::class)
            .play(step.soundId)
    }
}

private fun String.formatFor(viewer: Viewer): String {
    return this.replace("#USER#", viewer.displayName)
}
