package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.PostponedMessage

object PostponedMessageComparator : Comparator<PostponedMessage> {

    override fun compare(message1: PostponedMessage, message2: PostponedMessage): Int =
        if (message1.importance.value == message2.importance.value) compareContents(
            message1,
            message2
        )
        else message2.importance.value - message1.importance.value

    private fun compareContents(
        message1: PostponedMessage,
        message2: PostponedMessage
    ): Int {
        return "${message1.channel}+${message1.content}".compareTo("${message2.channel}+${message2.content}")
    }

}
