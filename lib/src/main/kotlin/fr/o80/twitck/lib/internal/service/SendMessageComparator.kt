package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.Deadline
import fr.o80.twitck.lib.api.bean.SendMessage

object SendMessageComparator : Comparator<SendMessage> {

    override fun compare(message1: SendMessage, message2: SendMessage): Int =
        when {
            // Immediate
            message1.deadline == Deadline.Immediate && message2.deadline == Deadline.Immediate ->
                compareContents(message1, message2)
            message1.deadline == Deadline.Immediate -> -1
            message2.deadline == Deadline.Immediate -> 1

            // Postponed
            message1.deadline is Deadline.Postponed && message2.deadline is Deadline.Postponed ->
                comparePostponed(message1, message2)
            message1.deadline is Deadline.Postponed -> -1
            message2.deadline is Deadline.Postponed -> 1

            // Repeated
            else -> compareContents(message1, message2)
        }

    private fun comparePostponed(
        message1: SendMessage,
        message2: SendMessage
    ): Int {
        val postponed1 = message1.deadline as Deadline.Postponed
        val postponed2 = message2.deadline as Deadline.Postponed

        return if (postponed1.importance.value == postponed2.importance.value) compareContents(message1, message2)
        else postponed2.importance.value - postponed1.importance.value
    }

    private fun compareContents(
        message1: SendMessage,
        message2: SendMessage
    ): Int {
        return "${message1.channel}+${message1.content}".compareTo("${message2.channel}+${message2.content}")
    }

}
