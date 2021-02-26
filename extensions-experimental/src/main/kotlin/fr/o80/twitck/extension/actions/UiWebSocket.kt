package fr.o80.twitck.extension.actions

import com.squareup.moshi.Moshi
import fr.o80.slobs.SlobsClient
import fr.o80.twitck.extension.actions.model.Config
import fr.o80.twitck.extension.actions.model.RemoteAction
import fr.o80.twitck.extension.actions.model.Scene
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.service.CommandTriggering
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.Duration

class UiWebSocket(
    private val channel: String,
    private val port: Int,
    private val store: RemoteActionStore,
    private val slobsClient: SlobsClient,
    private val commandTriggering: CommandTriggering,
    private val logger: Logger
) {

    private val sessions = mutableListOf<DefaultWebSocketServerSession>()

    private val moshi = Moshi.Builder().build()

    internal var messenger: Messenger? = null

    init {
        runBlocking {
            slobsClient.connect()
        }
    }

    fun start() {
        embeddedServer(Netty, port) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                webSocket("/actions") {
                    try {
                        sessions += this
                        while (true) {
                            when (val frame = incoming.receive()) {
                                is Frame.Binary -> logger.warn("Binary not handled")
                                is Frame.Close -> onClose(this, frame)
                                is Frame.Ping -> logger.warn("Ping not handled")
                                is Frame.Pong -> logger.warn("Pong not handled")
                                is Frame.Text -> onText(this, frame)
                            }
                        }
                    } catch (e: Exception) {
                        println(e.localizedMessage)
                    } finally {
                        sessions.remove(this)
                    }
                }
            }
        }.start(wait = false)
    }

    private suspend fun onText(session: DefaultWebSocketServerSession, frame: Frame.Text) {
        val request = frame.readText()
        when {
            request == "GetConfig" -> {
                onConfigRequested(session)
            }
            request.startsWith("AddAction:") -> {
                val newActionJson = request.substring(10)
                onNewAction(newActionJson)
            }
            request.startsWith("Command:") -> {
                val command = request.substring(8)
                onCommand(command)
            }
            request.startsWith("Message:") -> {
                val command = request.substring(8)
                onMessage(command)
            }
            request.startsWith("Scene:") -> {
                val sceneId = request.substring(6)
                onScene(sceneId)
            }
            else -> {
                logger.debug("Someone requested something weird: $request")
                session.send(Frame.Text("Unknown request"))
            }
        }
    }

    private suspend fun onConfigRequested(session: DefaultWebSocketServerSession) {
        session.send("Config:${getConfigJson()}")
    }

    private suspend fun getConfigJson(): String? {
        val actions = store.getActions()
        val scenes = slobsClient.getScenes().map { Scene(it.id, it.name) }

        val config = Config(actions, scenes)
        val adapter = moshi.adapter(Config::class.java)
        return adapter.toJson(config)
    }

    private suspend fun onNewAction(newActionJson: String) = withContext(Dispatchers.IO) {
        logger.debug("Someone requested the adding of action: $newActionJson")
        val adapter = moshi.adapter(RemoteAction::class.java)
        val action = adapter.fromJson(newActionJson)!!
        store.addAction(action)

        val configJson = getConfigJson()

        dispatch { otherSession ->
            otherSession.send("Config:$configJson")
        }
    }

    private fun onCommand(command: String) {
        println("Command received from UI: $command")
        // TODO Parser les options plutôt que de partir du principe qu'il n'y en a pas
        commandTriggering.sendCommand(command, emptyList())
    }

    private fun onMessage(message: String) {
        logger.debug("Message received from UI: $message")
        messenger?.sendImmediately(channel, message, CoolDown(Duration.ofSeconds(1)))
    }

    private suspend fun onScene(sceneId: String) {
        slobsClient.switchTo(sceneId)
    }

    private suspend fun dispatch(function: suspend (DefaultWebSocketServerSession) -> Unit) {
        sessions.forEach {
            function(it)
        }
    }

    private fun onClose(session: DefaultWebSocketServerSession, close: Frame.Close) {
        println("Closed: ${close.readReason().toString()}")
        sessions.remove(session)
    }

}
