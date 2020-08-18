package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler

interface Pipeline {
    fun interceptJoinEvent(joinHandler: JoinHandler)
    fun interceptMessageEvent(messageHandler: MessageHandler)
    fun interceptWhisperEvent(whisperHandler: WhisperHandler)
    fun requestChannel(channel: String)
}

