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
import fr.o80.twitck.lib.api.service.step.StepParam
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
        param: StepParam
    ) {
        steps.forEach { step ->
            Do exhaustive when (step) {
                is CommandStep -> execute(step, param)
                is MessageStep -> send(step, messenger, param)
                is OverlayPopupStep -> showPopup(step, param)
                is OverlayEventStep -> showEvent(step, param)
                is SoundStep -> play(step)
            }
        }
    }

    private fun execute(
        step: CommandStep,
        param: StepParam
    ) {
        val commandMessage = stepFormatter.format(step.command, param)
        val command = commandParser.parse(commandMessage)
            ?: throw IllegalArgumentException("Failed to convert ${step.command} to a valid command")
        commandTriggering.sendCommand(command.tag, command.options)
    }

    private fun send(
        step: MessageStep,
        messenger: Messenger,
        param: StepParam
    ) {
        val message = stepFormatter.format(step.message, param)
        messenger.sendImmediately(param.channel, message)
    }

    private fun showPopup(
        step: OverlayPopupStep,
        param: StepParam
    ) {
        if (overlay == null) {
            logger.error("Overlay popup steps require Overlay extension to work")
        } else {
            val text = stepFormatter.format(step.text, param)
            overlay.showImage(step.image, text, Duration.ofSeconds(step.seconds))
        }
    }

    private fun showEvent(
        step: OverlayEventStep,
        param: StepParam
    ) {
        if (overlay == null) {
            logger.error("Overlay event steps require Overlay extension to work")
        } else {
            val text = stepFormatter.format(step.text, param)
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
