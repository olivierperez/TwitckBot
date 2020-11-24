package fr.o80.twitck.extension.promotion

import fr.o80.twitck.lib.api.bean.CommandEvent
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.utils.skip
import java.time.Duration

const val SHOUT_OUT_COMMAND = "!shoutout"

class ViewerPromotionCommand(
    private val channel: String,
    private val storage: StorageExtension
) {

    private val namespace: String = ViewerPromotion::class.java.name

    fun interceptCommandEvent(messenger: Messenger, commandEvent: CommandEvent): CommandEvent {
        when (commandEvent.command.tag) {
            SHOUT_OUT_COMMAND -> recordShoutOut(messenger, commandEvent)
        }
        return commandEvent
    }

    fun interceptWhisperCommandEvent(
        messenger: Messenger,
        commandEvent: CommandEvent
    ): CommandEvent {
        when (commandEvent.command.tag) {
            SHOUT_OUT_COMMAND -> shoutOut(commandEvent, messenger)
        }
        return commandEvent
    }

    private fun recordShoutOut(messenger: Messenger, commandEvent: CommandEvent) {
        if (commandEvent.command.options.size < 2) {
            messenger.sendImmediately(
                commandEvent.channel,
                "usage: $SHOUT_OUT_COMMAND <login> <message>"
            )
            return
        }
        val login = commandEvent.command.options[0]
        val message = commandEvent.command.options.skip(1).joinToString(" ")

        if (storage.hasUserInfo(login)) {
            storage.putUserInfo(login, namespace, SHOUT_OUT_COMMAND, message)
        }
    }

    private fun shoutOut(commandEvent: CommandEvent, messenger: Messenger) {
        if (commandEvent.command.tag != SHOUT_OUT_COMMAND) return

        if (commandEvent.command.options.size != 1) {
            messenger.sendImmediately(channel, "usage: $SHOUT_OUT_COMMAND <login>")
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
