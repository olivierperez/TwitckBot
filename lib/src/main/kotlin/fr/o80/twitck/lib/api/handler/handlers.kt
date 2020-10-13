package fr.o80.twitck.lib.api.handler

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.FollowsEvent
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.bean.SubscriptionEvent
import fr.o80.twitck.lib.api.bean.WhisperEvent
import fr.o80.twitck.lib.api.service.Messenger

typealias JoinHandler = (messenger: Messenger, joinEvent: JoinEvent) -> JoinEvent
typealias MessageHandler = (messenger: Messenger, messageEvent: MessageEvent) -> MessageEvent
typealias CommandHandler = (messenger: Messenger, commandEvent: CommandEvent) -> CommandEvent
typealias WhisperHandler = (messenger: Messenger, whisper: WhisperEvent) -> Unit
typealias FollowsHandler = (messenger: Messenger, follows: FollowsEvent) -> FollowsEvent
typealias SubscriptionsHandler = (messenger: Messenger, follow: SubscriptionEvent) -> SubscriptionEvent
