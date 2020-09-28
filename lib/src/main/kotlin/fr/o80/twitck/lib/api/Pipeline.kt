package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.handler.CommandHandler
import fr.o80.twitck.lib.api.handler.FollowHandler
import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler

interface Pipeline {
    fun interceptJoinEvent(joinHandler: JoinHandler)
    fun interceptMessageEvent(messageHandler: MessageHandler)
    fun interceptCommandEvent(commandHandler: CommandHandler)
    fun interceptWhisperEvent(whisperHandler: WhisperHandler)
    fun interceptFollowHandler(followHandler: FollowHandler)
    fun requestChannel(channel: String)
}

