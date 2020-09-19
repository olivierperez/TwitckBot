package fr.o80.twitck.lib.api

import fr.o80.twitck.lib.api.bean.SendMessage

interface Messenger {
    fun send(message: SendMessage)
}
