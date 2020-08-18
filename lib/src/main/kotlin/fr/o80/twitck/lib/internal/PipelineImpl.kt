package fr.o80.twitck.lib.internal

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler

internal class PipelineImpl : Pipeline {
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