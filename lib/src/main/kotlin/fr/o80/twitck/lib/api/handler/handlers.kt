package fr.o80.twitck.lib.api.handler

import fr.o80.twitck.lib.api.Messenger
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.bean.WhisperEvent

typealias JoinHandler = (messenger: Messenger, joinEvent: JoinEvent) -> JoinEvent
typealias MessageHandler = (messenger: Messenger, messageEvent: MessageEvent) -> MessageEvent
typealias CommandHandler = (messenger: Messenger, commandEvent: CommandEvent) -> CommandEvent
typealias WhisperHandler = (messenger: Messenger, whisper: WhisperEvent) -> Unit
