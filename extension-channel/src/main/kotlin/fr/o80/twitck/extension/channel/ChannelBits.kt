package fr.o80.twitck.extension.channel

import fr.o80.twitck.lib.api.bean.event.BitsEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.api.service.step.ActionStep
import fr.o80.twitck.lib.api.service.step.StepParams
import fr.o80.twitck.lib.api.service.step.StepsExecutor

class ChannelBits(
    private val steps: List<ActionStep>,
    private val stepsExecutor: StepsExecutor,
    private val logger: Logger
) {
    fun interceptBitsEvent(messenger: Messenger, bitsEvent: BitsEvent): BitsEvent {
        logger.debug("Bits intercepted in Channel extension: $bitsEvent")
        stepsExecutor.execute(
            steps = steps,
            messenger = messenger,
            params = StepParams.fromBits(bitsEvent)
        )

        return bitsEvent
    }

}
