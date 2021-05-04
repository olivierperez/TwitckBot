package fr.o80.twitck.extension.market

import fr.o80.twitck.lib.api.bean.event.RewardEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.api.service.step.StepParams
import fr.o80.twitck.lib.api.service.step.StepsExecutor

class MarketRewards(
    private val rewards: List<MarketReward>,
    private val stepsExecutor: StepsExecutor,
    private val logger: Logger
) {

    fun interceptRewardEvent(messenger: Messenger, rewardEvent: RewardEvent): RewardEvent {
        logger.debug("Reward received by Market: $rewardEvent")

        rewards.firstOrNull { it.id == rewardEvent.rewardId }?.let { reward ->
            stepsExecutor.execute(
                steps = reward.steps,
                messenger = messenger,
                params = StepParams.fromReward(rewardEvent)
            )
        }

        return rewardEvent
    }
}
