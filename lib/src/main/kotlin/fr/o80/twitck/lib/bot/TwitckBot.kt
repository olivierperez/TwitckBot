package fr.o80.twitck.lib.bot

interface TwitckBot {

    fun connectToServer()

    fun join(
        channel: String
    )

    fun sendLine(
        line: String
    )

    fun send(
        channel: String,
        message: String
    )

    fun onMessage(
        channel: String,
        sender: String,
        login: String,
        hostname: String,
        message: String
    )

    fun onJoin(
        channel: String,
        sender: String,
        login: String,
        hostname: String
    )

}