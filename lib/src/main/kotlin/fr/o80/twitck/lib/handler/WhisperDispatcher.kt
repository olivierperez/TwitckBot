package fr.o80.twitck.lib.handler

import fr.o80.twitck.lib.bot.TwitckBot

typealias WhisperHandler = (bot: TwitckBot, whisper: WhisperEvent) -> Unit

class WhisperDispatcher(
    private val bot: TwitckBot,
    private val handlers: List<WhisperHandler>
) {
    fun dispatch(event: WhisperEvent) {
        handlers.forEach { handler ->
            handler(bot, event)
        }
    }
}

class WhisperEvent(
    val destination: String,
    val login: String,
    val message: String
)
