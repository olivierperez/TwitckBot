package fr.o80.twitck.extension.channel

import fr.o80.twitck.lib.api.bean.event.FollowsEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.step.ActionStep
import fr.o80.twitck.lib.api.service.step.StepParam
import fr.o80.twitck.lib.api.service.step.StepsExecutor

class ChannelFollows(
    private val follows: List<ActionStep>,
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
