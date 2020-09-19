package fr.o80.twitck.lib.api.service

import fr.o80.twitck.lib.api.bean.SendMessage

interface Messenger {
    fun send(message: SendMessage)
}
