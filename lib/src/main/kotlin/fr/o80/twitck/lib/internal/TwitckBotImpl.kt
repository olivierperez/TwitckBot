package fr.o80.twitck.lib.internal

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Command
import fr.o80.twitck.lib.api.bean.TwitckConfiguration
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.CommandEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.handler.*
import fr.o80.twitck.lib.internal.service.MessengerImpl
import fr.o80.twitck.lib.internal.service.Ping
import fr.o80.twitck.lib.internal.service.line.*
import fr.o80.twitck.lib.internal.service.topic.NgrokTunnel
import fr.o80.twitck.lib.internal.service.topic.SecretHolder
import fr.o80.twitck.lib.internal.service.topic.TopicManager
import fr.o80.twitck.lib.internal.service.topic.WebhooksServer
import org.jibble.pircbot.PircBot
import kotlin.system.exitProcess

internal class TwitckBotImpl(
    private val configuration: TwitckConfiguration
) : PircBot(), TwitckBot {

    private val initializer = TwitckInitializer()

    private val ping = Ping(this)

    // TODO OPZ Afin que le Messenger arrête de connaitre le Bot
    // TODO OPZ Il faut que le messenger gère une BlockingQueue, et que le bot ait un thread qui dépile cette Queue
    private val messenger: Messenger = MessengerImpl(
        this,
        configuration.coolDownManager
    )

    private val commandDispatcher = CommandDispatcher(messenger, configuration.commandHandlers)

    private val privMsgLineHandler = PrivMsgLineInterpreter(
        messenger,
        configuration.commandParser,
        MessageDispatcher(messenger, configuration.messageHandlers),
        commandDispatcher
    )

    private val raidInterpreter = RaidInterpreter(
        messenger,
        RaidDispatcher(messenger, configuration.raidHandlers),
        configuration.loggerFactory.getLogger(TwitckBotImpl::class)
    )

    private val hostUserId: String = configuration.twitchApi.getUser(configuration.hostName).id

    private val joinLineHandler =
        JoinLineInterpreter(this, JoinDispatcher(messenger, configuration.joinHandlers))

    private val whisperLineHandler =
        WhisperLineInterpreter(
            messenger,
            configuration.commandParser,
            WhisperDispatcher(messenger, configuration.whisperHandlers),
            WhisperCommandDispatcher(messenger, configuration.whisperCommandHandlers)
        )

    private val autoJoiner =
        AutoJoiner(this, configuration.requestedChannels, configuration.loggerFactory)

    private val lineInterpreters =
        LineInterpreters(privMsgLineHandler, joinLineHandler, whisperLineHandler, raidInterpreter)

    private val logger: Logger = configuration.loggerFactory.getLogger(TwitckBotImpl::class)

    private val topicManager = TopicManager(
        userId = hostUserId,
        api = configuration.twitchApi,
        // TODO OPZ BotHusky ne devrait pas se trouver dans la lib
        ngrokTunnel = NgrokTunnel("BotHusky", 8080),
        secret = SecretHolder.secret,
        webhooksServer = WebhooksServer(
            followsDispatcher = FollowsDispatcher(messenger, configuration.followsHandlers),
            subscriptionsDispatcher = SubscriptionsDispatcher(
                messenger,
                configuration.subscriptionsHandlers
            ),
            secret = SecretHolder.secret,
            loggerFactory = configuration.loggerFactory
        ),
        loggerFactory = configuration.loggerFactory
    )

    override fun connectToServer() {
        try {
            logger.info("Attempting to connect to irc.twitch.tv...")

            connect(HOST, PORT, "oauth:${configuration.oauthToken}")

            logger.info("Requesting twitch membership capability for NAMES/JOIN/PART/MODE messages...")
            sendRawLine(SERVER_MEMREQ)

            logger.info("Requesting twitch commands capability for NOTICE/HOSTTARGET/CLEARCHAT/USERSTATE messages... ")
            sendRawLine(SERVER_CMDREQ)

            logger.info("Requesting twitch tags capability for PRIVMSG/USERSTATE/GLOBALUSERSTATE messages... ")
            sendRawLine(SERVER_TAGREG)

            while (!initializer.initialized) {
                Thread.sleep(1000)
                if (!initializer.initialized) logger.debug("Not yet initialized")
            }

            autoJoiner.join()
            topicManager.subscribe()
            configuration.commandsFromExtension.listener = ::listenCommandsFromExtension
        } catch (e: Exception) {
            logger.error("Something gone wrong at startup", e)
            exitProcess(-1)
        }
    }

    override fun join(channel: String) {
        logger.info("Attempting to join channel $channel")
        super.joinChannel(channel)
    }

    override fun handleLine(line: String?) {
        logger.trace("Handle line: $line")
        super.handleLine(line)
        line ?: return

        initializer.handleLine(line)
        ping.handleLine(line)
        lineInterpreters.interpretLine(line)
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
        logger.debug("Message received from $sender on channel $channel: $message")
    }

    override fun onJoin(
        channel: String,
        sender: String,
        login: String,
        hostname: String
    ) {
        super.onJoin(channel, sender, login, hostname)
        logger.trace("$sender join the channel $channel")
    }

    private fun listenCommandsFromExtension(command: String) {
        commandDispatcher.dispatch(
            CommandEvent(
                command = Command(command),
                messenger = messenger,
                channel = "#${configuration.hostName}",
                bits = 0,
                viewer = Viewer(
                    login = configuration.hostName,
                    displayName = configuration.hostName,
                    badges = listOf(Badge.BROADCASTER),
                    userId = hostUserId,
                    color = "#000000"
                )
            )
        )
    }

}
