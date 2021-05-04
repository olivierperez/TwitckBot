package fr.o80.twitck.lib.api.handler

import fr.o80.twitck.lib.api.bean.event.BitsEvent
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.bean.event.FollowsEvent
import fr.o80.twitck.lib.api.bean.event.JoinEvent
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.bean.event.RaidEvent
import fr.o80.twitck.lib.api.bean.event.RewardEvent
import fr.o80.twitck.lib.api.bean.event.WhisperEvent
import fr.o80.twitck.lib.api.bean.subscription.SubscriptionEvent
import fr.o80.twitck.lib.api.service.Messenger

typealias BitsHandler = (messenger: Messenger, bitsEvent: BitsEvent) -> BitsEvent
typealias CommandHandler = (messenger: Messenger, commandEvent: CommandEvent) -> CommandEvent
typealias FollowsHandler = (messenger: Messenger, follows: FollowsEvent) -> FollowsEvent
typealias JoinHandler = (messenger: Messenger, joinEvent: JoinEvent) -> JoinEvent
typealias MessageHandler = (messenger: Messenger, messageEvent: MessageEvent) -> MessageEvent
typealias RaidHandler = (messenger: Messenger, raid: RaidEvent) -> RaidEvent
typealias RewardHandler = (messenger: Messenger, rewardEvent: RewardEvent) -> RewardEvent?
typealias SubscriptionsHandler = (messenger: Messenger, follow: SubscriptionEvent) -> SubscriptionEvent
typealias WhisperHandler = (messenger: Messenger, whisper: WhisperEvent) -> Unit
