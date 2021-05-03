package fr.o80.twitck.extension.channel

import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.step.ActionStep
import fr.o80.twitck.lib.api.service.step.StepParams
import fr.o80.twitck.lib.api.service.step.StepsExecutor

class ChannelCommands(
    private val commandConfigs: Map<String, List<ActionStep>>,
    private val stepsExecutor: StepsExecutor
) {

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        commandConfigs[commandEvent.command.tag]?.let { commandConfig ->
            stepsExecutor.execute(
                commandConfig,
                messenger,
                StepParams.fromCommand(commandEvent)
            )
        }

        return commandEvent
    }
}
