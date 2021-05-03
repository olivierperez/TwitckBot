package fr.o80.twitck.lib.internal.service.step

import fr.o80.twitck.lib.api.extension.OverlayEvent
import fr.o80.twitck.lib.api.extension.OverlayExtension
import fr.o80.twitck.lib.api.extension.SoundExtension
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.CommandTriggering
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.api.service.step.ActionStep
import fr.o80.twitck.lib.api.service.step.CommandStep
import fr.o80.twitck.lib.api.service.step.MessageStep
import fr.o80.twitck.lib.api.service.step.OverlayEventStep
import fr.o80.twitck.lib.api.service.step.OverlayPopupStep
import fr.o80.twitck.lib.api.service.step.SoundStep
import fr.o80.twitck.lib.api.service.step.StepParams
import fr.o80.twitck.lib.api.service.step.StepsExecutor
import fr.o80.twitck.lib.utils.Do
import java.time.Duration

internal class StepsExecutorImpl(
    private val commandTriggering: CommandTriggering,
    private val commandParser: CommandParser,
    private val stepFormatter: StepFormatter,
    private val logger: Logger,
    private val overlay: OverlayExtension?,
    private val sound: SoundExtension?,
) : StepsExecutor {

    override fun execute(
        steps: List<ActionStep>,
        messenger: Messenger,
        params: StepParams
    ) {
        steps.forEach { step ->
            Do exhaustive when (step) {
                is CommandStep -> execute(step, params)
                is MessageStep -> send(step, messenger, params)
                is OverlayPopupStep -> showPopup(step, params)
                is OverlayEventStep -> showEvent(step, params)
                is SoundStep -> play(step)
            }
        }
    }

    private fun execute(
        step: CommandStep,
        params: StepParams
    ) {
        val commandMessage = stepFormatter.format(step.command, params)
        val command = commandParser.parse(commandMessage)
            ?: throw IllegalArgumentException("Failed to convert ${step.command} to a valid command")
        commandTriggering.sendCommand(command.tag, command.options)
    }

    private fun send(
        step: MessageStep,
        messenger: Messenger,
        params: StepParams
    ) {
        val message = stepFormatter.format(step.message, params)
        messenger.sendImmediately(params.channel, message)
    }

    private fun showPopup(
        step: OverlayPopupStep,
        params: StepParams
    ) {
        if (overlay == null) {
            logger.error("Overlay popup steps require Overlay extension to work")
        } else {
            val text = stepFormatter.format(step.text, params)
            overlay.showImage(step.image, text, Duration.ofSeconds(step.seconds))
        }
    }

    private fun showEvent(
        step: OverlayEventStep,
        params: StepParams
    ) {
        if (overlay == null) {
            logger.error("Overlay event steps require Overlay extension to work")
        } else {
            val text = stepFormatter.format(step.text, params)
            overlay.onEvent(OverlayEvent(text))
        }
    }

    private fun play(step: SoundStep) {
        if (sound == null) {
            logger.error("Sound steps require Sound extension to work")
        } else {
            sound.play(step.soundId)
        }
    }

}
