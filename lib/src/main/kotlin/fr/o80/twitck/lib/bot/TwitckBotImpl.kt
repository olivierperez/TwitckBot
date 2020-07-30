package fr.o80.twitck.lib.bot

import fr.o80.twitck.lib.HOST
import fr.o80.twitck.lib.PORT
import fr.o80.twitck.lib.SERVER_CMDREQ
import fr.o80.twitck.lib.SERVER_MEMREQ
import fr.o80.twitck.lib.SERVER_TAGREG
import fr.o80.twitck.lib.TwitckInitializer
import fr.o80.twitck.lib.bean.TwitckConfiguration
import fr.o80.twitck.lib.handler.AutoJoiner
import fr.o80.twitck.lib.handler.JoinDispatcher
import fr.o80.twitck.lib.handler.MessageDispatcher
import fr.o80.twitck.lib.handler.WhisperDispatcher
import fr.o80.twitck.lib.service.Ping
import fr.o80.twitck.lib.service.line.JoinLineHandler
import fr.o80.twitck.lib.service.line.LineInterpreter
import fr.o80.twitck.lib.service.line.PrivMsgLineHandler
import fr.o80.twitck.lib.service.line.WhisperLineHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.jibble.pircbot.PircBot

class TwitckBotImpl(
    private val oauthToken: String,
    private val configuration: TwitckConfiguration
) : PircBot(), TwitckBot {

    private val initializer = TwitckInitializer()

    private val ping = Ping(this)

    private val privMsgLineHandler = PrivMsgLineHandler(this, MessageDispatcher(this, configuration.messageHandlers))
    private val joinLineHandler = JoinLineHandler(this, JoinDispatcher(this, configuration.joinHandlers))
    private val whisperLineHandler = WhisperLineHandler(this, WhisperDispatcher(this, configuration.whisperHandlers))

    private val autoJoiner = AutoJoiner(this, configuration.requestedChannels)

    private val lineInterpreter = LineInterpreter(privMsgLineHandler, joinLineHandler, whisperLineHandler)

    override suspend fun connectToServer() = coroutineScope {
        println("Attempting to connect to irc.twitch.tv...")

        connect(HOST, PORT, "oauth:$oauthToken")

        println("Requesting twitch membership capability for NAMES/JOIN/PART/MODE messages...")
        sendRawLine(SERVER_MEMREQ)

        println("Requesting twitch commands capability for NOTICE/HOSTTARGET/CLEARCHAT/USERSTATE messages... ")
        sendRawLine(SERVER_CMDREQ)

        println("Requesting twitch tags capability for PRIVMSG/USERSTATE/GLOBALUSERSTATE messages... ")
        sendRawLine(SERVER_TAGREG)

        while (!initializer.initialized) {
            delay(1000)
            if (!initializer.initialized) println("Not yet initialized")
        }

        autoJoiner.join()
    }

    override fun join(channel: String) {
        println("Attempting to join channel $channel")
        super.joinChannel(channel)
    }

    override fun handleLine(line: String?) {
        println("Handle line: $line")
        super.handleLine(line)
        line ?: return

        initializer.handleLine(line)
        ping.handleLine(line)
        lineInterpreter.handle(line)
    }

    override fun sendLine(line: String) {
        sendRawLine(line)
    }

    override fun send(channel: String, message: String) {
        sendMessage(channel, message)
    }

    override fun onMessage(
        channel: String,
        sender: String,
        login: String,
        hostname: String,
        message: String
    ) {
        super.onMessage(channel, sender, login, hostname, message)
        println("Message received from $sender on channel $channel: $message")
    }

    override fun onJoin(
        channel: String,
        sender: String,
        login: String,
        hostname: String
    ) {
        super.onJoin(channel, sender, login, hostname)
        println("$sender join the channel $channel")
    }
}