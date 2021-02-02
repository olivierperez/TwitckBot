package fr.o80.twitck.lib.api.service.step

import fr.o80.twitck.lib.api.service.Messenger

interface StepsExecutor {
    fun execute(
        steps: List<CommandStep>,
        messenger: Messenger,
        param: StepParam
    )
}

class StepParam(
    val channel: String,
    val viewerName: String
)
