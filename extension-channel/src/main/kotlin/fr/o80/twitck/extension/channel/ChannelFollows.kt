package fr.o80.twitck.extension.channel

import fr.o80.twitck.lib.api.bean.event.FollowsEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.step.CommandStep
import fr.o80.twitck.lib.api.service.step.StepParam
import fr.o80.twitck.lib.api.service.step.StepsExecutor

class ChannelFollows(
    private val follows: List<CommandStep>,
    private val stepsExecutor: StepsExecutor
) {

    fun interceptFollowEvent(messenger: Messenger, followsEvent: FollowsEvent): FollowsEvent {
        followsEvent.followers.data.forEach { newFollower ->
            val param = StepParam("#${newFollower.toName.toLowerCase()}", newFollower.fromName)
            stepsExecutor.execute(follows, messenger, param)
        }
        return followsEvent
    }

}
