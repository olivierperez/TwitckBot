package fr.o80.twitck.extension.channel

import fr.o80.twitck.extension.channel.config.CommandStep
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.service.Messenger

class ChannelCommands(
    private val commandConfigs: Map<String, List<CommandStep>>,
    private val stepsExecutor: StepsExecutor
) {

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        commandConfigs[commandEvent.command.tag]?.let { commandConfig ->
            stepsExecutor.execute(
                commandConfig,
                messenger,
                StepParam(
                    commandEvent.channel,
                    commandEvent.viewer.displayName
                )
            )
        }

        return commandEvent
    }
}
