package fr.o80.twitck.lib.api.service.step

import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.utils.skip

interface StepsExecutor {
    fun execute(
        steps: List<ActionStep>,
        messenger: Messenger,
        param: StepParam
    )
}

class StepParam(
    val channel: String,
    val viewerName: String,
    val params: List<String> = emptyList()
) {
    companion object {
        fun fromCommand(commandEvent: CommandEvent, skipOptions: Int = 0): StepParam {
            return StepParam(
                commandEvent.channel,
                commandEvent.viewer.displayName,
                commandEvent.command.options.skip(skipOptions)
            )
        }
    }
}
