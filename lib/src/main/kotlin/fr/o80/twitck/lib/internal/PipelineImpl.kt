package fr.o80.twitck.lib.internal

import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.handler.BitsHandler
import fr.o80.twitck.lib.api.handler.CommandHandler
import fr.o80.twitck.lib.api.handler.FollowsHandler
import fr.o80.twitck.lib.api.handler.JoinHandler
import fr.o80.twitck.lib.api.handler.MessageHandler
import fr.o80.twitck.lib.api.handler.RaidHandler
import fr.o80.twitck.lib.api.handler.SubscriptionsHandler
import fr.o80.twitck.lib.api.handler.WhisperHandler

internal interface PipelineProvider {
    val bitsHandlers: MutableList<BitsHandler>
    val commandHandlers: MutableList<CommandHandler>
    val followsHandlers: MutableList<FollowsHandler>
    val joinHandlers: MutableList<JoinHandler>
    val messageHandlers: MutableList<MessageHandler>
    val raidHandlers: MutableList<RaidHandler>
    val subscriptionsHandlers: MutableList<SubscriptionsHandler>
    val whisperCommandHandlers: MutableList<CommandHandler>
    val whisperHandlers: MutableList<WhisperHandler>

    val requestedChannels: MutableSet<String>
}

internal class PipelineImpl : Pipeline, PipelineProvider {
    override val bitsHandlers: MutableList<BitsHandler> = mutableListOf()
    override val commandHandlers: MutableList<CommandHandler> = mutableListOf()
    override val followsHandlers: MutableList<FollowsHandler> = mutableListOf()
    override val joinHandlers: MutableList<JoinHandler> = mutableListOf()
    override val messageHandlers: MutableList<MessageHandler> = mutableListOf()
    override val raidHandlers: MutableList<RaidHandler> = mutableListOf()
    override val subscriptionsHandlers: MutableList<SubscriptionsHandler> = mutableListOf()
    override val whisperCommandHandlers: MutableList<CommandHandler> = mutableListOf()
    override val whisperHandlers: MutableList<WhisperHandler> = mutableListOf()

    override val requestedChannels: MutableSet<String> = mutableSetOf()

    override fun interceptBitsEvent(bitsHandler: BitsHandler) {
        bitsHandlers += bitsHandler
    }

    override fun interceptJoinEvent(joinHandler: JoinHandler) {
        joinHandlers += joinHandler
    }

    override fun interceptMessageEvent(messageHandler: MessageHandler) {
        messageHandlers += messageHandler
    }

    override fun interceptCommandEvent(commandHandler: CommandHandler) {
        commandHandlers += commandHandler
    }

    override fun interceptWhisperEvent(whisperHandler: WhisperHandler) {
        whisperHandlers += whisperHandler
    }

    override fun interceptWhisperCommandEvent(commandHandler: CommandHandler) {
        whisperCommandHandlers += commandHandler
    }

    override fun interceptFollowEvent(followsHandler: FollowsHandler) {
        followsHandlers += followsHandler
    }

    override fun interceptSubscriptionEvent(subscriptionsHandler: SubscriptionsHandler) {
        subscriptionsHandlers += subscriptionsHandler
    }

    override fun interceptRaidEvent(raidHandler: RaidHandler) {
        raidHandlers += raidHandler
    }

    override fun requestChannel(channel: String) {
        requestedChannels += channel
    }
}
