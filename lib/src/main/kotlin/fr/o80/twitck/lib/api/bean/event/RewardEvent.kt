package fr.o80.twitck.lib.api.bean.event

import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.service.Messenger

data class RewardEvent(
    val messenger: Messenger,
    val channel: String,
    val rewardId: String,
    val message: String,
    val viewer: Viewer
)
