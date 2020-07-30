package fr.o80.twitck.lib

import fr.o80.twitck.lib.handler.JoinHandler
import fr.o80.twitck.lib.handler.MessageHandler
import fr.o80.twitck.lib.handler.WhisperHandler

interface Pipeline {
    fun interceptJoinEvent(joinHandler: JoinHandler)
    fun interceptMessageEvent(messageHandler: MessageHandler)
    fun interceptWhisperEvent(whisperHandler: WhisperHandler)
    fun requestChannel(channel: String)
}

class PipelineImpl : Pipeline {
    val joinHandlers: MutableList<JoinHandler> = mutableListOf()
    val messageHandlers: MutableList<MessageHandler> = mutableListOf()
    val whisperHandlers: MutableList<WhisperHandler> = mutableListOf()
    val requestedChannels: MutableSet<String> = mutableSetOf()

    override fun interceptJoinEvent(joinHandler: JoinHandler) {
        joinHandlers += joinHandler
    }

    override fun interceptMessageEvent(messageHandler: MessageHandler) {
        messageHandlers += messageHandler
    }

    override fun interceptWhisperEvent(whisperHandler: WhisperHandler) {
        whisperHandlers += whisperHandler
    }

    override fun requestChannel(channel: String) {
        requestedChannels += channel
    }
}
