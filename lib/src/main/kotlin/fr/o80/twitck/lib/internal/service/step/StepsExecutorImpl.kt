package fr.o80.twitck.lib.internal.service.step

import fr.o80.twitck.lib.api.extension.ExtensionProvider
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.step.CommandStep
import fr.o80.twitck.lib.api.service.step.MessageStep
import fr.o80.twitck.lib.api.service.step.OverlayStep
import fr.o80.twitck.lib.api.service.step.SoundStep
import fr.o80.twitck.lib.api.service.step.StepParam
import fr.o80.twitck.lib.api.service.step.StepsExecutor
import java.time.Duration

internal class StepsExecutorImpl(
    private val extensionProvider: ExtensionProvider
) : StepsExecutor {

    override fun execute(
        steps: List<CommandStep>,
        messenger: Messenger,
        param: StepParam
    ) {
        steps.forEach { step ->
            when (step) {
                is MessageStep -> send(step, messenger, param)
                is OverlayStep -> show(step)
                is SoundStep -> play(step)
            }
        }
    }

    private fun send(
        step: MessageStep,
        messenger: Messenger,
        param: StepParam
    ) {
        val message = step.message.formatFor(param.viewerName)
        messenger.sendImmediately(param.channel, message)
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

private fun String.formatFor(viewerName: String): String {
    return this.replace("#USER#", viewerName)
}
