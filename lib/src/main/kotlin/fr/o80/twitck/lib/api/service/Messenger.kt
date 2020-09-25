package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.Importance

interface Messenger {

    fun sendImmediately(
        channel: String,
        content: String,
        coolDown: CoolDown? = null
    )

    fun sendWhenAvailable(
        channel: String,
        content: String,
        importance: Importance,
        coolDown: CoolDown? = null
    )

    fun sendLine(line: String)

    fun whisper(channel: String, recipient: String, message: String)

}
