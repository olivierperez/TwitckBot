package fr.o80.twitck.lib.api.handler

import fr.o80.twitck.lib.api.bean.MessageEvent
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.JoinEvent
import fr.o80.twitck.lib.api.bean.WhisperEvent

typealias JoinHandler = (bot: TwitckBot, joinEvent: JoinEvent) -> JoinEvent
typealias MessageHandler = (bot: TwitckBot, messageEvent: MessageEvent) -> MessageEvent
typealias CommandHandler = (bot: TwitckBot, commandEvent: CommandEvent) -> CommandEvent
typealias WhisperHandler = (bot: TwitckBot, whisper: WhisperEvent) -> Unit
