package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.BitsEvent
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.bean.event.MessageEvent
import fr.o80.twitck.lib.api.bean.event.RewardEvent
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.handler.BitsDispatcher
import fr.o80.twitck.lib.internal.handler.CommandDispatcher
import fr.o80.twitck.lib.internal.handler.MessageDispatcher
import fr.o80.twitck.lib.internal.handler.RewardDispatcher

internal class PrivMsgLineInterpreter(
    private val messenger: Messenger,
    private val commandParser: CommandParser,
    private val bitsDispatcher: BitsDispatcher,
    private val messageDispatcher: MessageDispatcher,
    private val commandDispatcher: CommandDispatcher,
    private val rewardDispatcher: RewardDispatcher,
    private val logger: Logger
) : LineInterpreter {

    private val regex =
        "^@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG (#[^ ]+) :(.+)$".toRegex()

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val tags = Tags.from(matchResult.groupValues[1])
            val user = matchResult.groupValues[2]
            val channel = matchResult.groupValues[3]
            val message = matchResult.groupValues[4]

            val viewer = Viewer(
                login = user,
                displayName = tags.displayName,
                badges = tags.badges,
                userId = tags.userId,
                color = tags.color
            )

            val command = commandParser.parse(message)

            tags.bits.takeIf { it > 0 }?.let { bits ->
                logger.debug("Bits have been detected: \n=>$viewer\n=>$tags\n===================")
                dispatchBits(channel, bits, viewer)
            }

            tags.customRewardId?.let { rewardId ->
                logger.debug("Reward claimed: $rewardId\n=>$viewer\n=>$tags\n===================")
                dispatchReward(channel, rewardId, message, viewer)
            }

            when {
                command != null -> dispatchCommand(channel, command, tags, viewer)
                else -> dispatchMessage(channel, message, viewer)
            }
        }
    }

    private fun dispatchBits(
        channel: String,
        bits: Int,
        viewer: Viewer
    ) {
        bitsDispatcher.dispatch(
            BitsEvent(
                messenger,
                channel,
                bits,
                viewer
            )
        )
    }

    private fun dispatchCommand(
        channel: String,
        command: Command,
        tags: Tags,
        viewer: Viewer
    ) {
        commandDispatcher.dispatch(
            CommandEvent(
                messenger,
                channel,
                command,
                tags.bits,
                viewer
            )
        )
    }

    private fun dispatchMessage(
        channel: String,
        msg: String,
        viewer: Viewer
    ) {
        messageDispatcher.dispatch(
            MessageEvent(
                messenger,
                channel,
                msg,
                viewer
            )
        )
    }

    private fun dispatchReward(
        channel: String,
        rewardId: String,
        message: String,
        viewer: Viewer
    ) {
        rewardDispatcher.dispatch(
            RewardEvent(messenger, channel, rewardId, message, viewer)
        )
    }
}
