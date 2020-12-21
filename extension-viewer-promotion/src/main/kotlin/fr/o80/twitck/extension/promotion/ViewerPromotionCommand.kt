package fr.o80.twitck.extension.promotion

import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.extension.PointsManager
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.utils.skip
import java.time.Duration

const val SHOUT_OUT_COMMAND = "!shoutout"
const val RECORDING_COST = 200
const val SHOUT_OUT_COST = 50

class ViewerPromotionCommand(
    private val channel: String,
    private val storage: StorageExtension,
    private val points: PointsManager,
    private val messages: ViewerPromotionMessages
) {

    private val namespace: String = ViewerPromotion::class.java.name

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            SHOUT_OUT_COMMAND -> handleShoutOutCommand(messenger, commandEvent)
        }
        return commandEvent
    }

    private fun handleShoutOutCommand(messenger: Messenger, commandEvent: CommandEvent) {
        when (commandEvent.command.options.size) {
            0 -> showUsage(messenger, commandEvent)
            1 -> shoutOut(messenger, commandEvent)
            else -> recordShoutOut(messenger, commandEvent)
        }
    }

    private fun showUsage(messenger: Messenger, commandEvent: CommandEvent) {
        messenger.sendImmediately(
            commandEvent.channel,
            messages.usage
        )
    }

    fun interceptWhisperCommandEvent(
        messenger: Messenger,
        commandEvent: CommandEvent
    ): CommandEvent {
        when (commandEvent.command.tag) {
            SHOUT_OUT_COMMAND -> shoutOut(messenger, commandEvent)
        }
        return commandEvent
    }

    private fun recordShoutOut(messenger: Messenger, commandEvent: CommandEvent) {
        if(!points.consumePoints(commandEvent.viewer.login, RECORDING_COST)) {
            val message = messages.noPointsEnough.replace("#USER#", commandEvent.viewer.login)
            messenger.sendImmediately(channel, message)
            return
        }

        val login = commandEvent.command.options[0]
        val message = commandEvent.command.options.skip(1).joinToString(" ")

        if (storage.hasUserInfo(login)) {
            storage.putUserInfo(login, namespace, SHOUT_OUT_COMMAND, message)
            messenger.sendImmediately(channel, messages.shoutOutRecorded)
        }
    }

    private fun shoutOut(messenger: Messenger, commandEvent: CommandEvent) {
        if(!points.consumePoints(commandEvent.viewer.login, SHOUT_OUT_COST)) {
            val message = messages.noPointsEnough.replace("#USER#", commandEvent.viewer.login)
            messenger.sendImmediately(channel, message)
            return
        }

        val login = commandEvent.command.options[0]

        if (storage.hasUserInfo(login)) {
            storage.getUserInfo(login, namespace, SHOUT_OUT_COMMAND)?.let { message ->
                messenger.sendImmediately(
                    channel,
                    message,
                    CoolDown(Duration.ofSeconds(60))
                )
            }
        }
    }

}
