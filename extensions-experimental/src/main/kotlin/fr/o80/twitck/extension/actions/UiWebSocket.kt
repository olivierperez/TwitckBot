package fr.o80.twitck.extension.actions

import com.squareup.moshi.Moshi
import fr.o80.twitck.lib.api.service.log.Logger
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import java.time.Duration

class UiWebSocket(
    private val store: RemoteActionStore,
    private val logger: Logger
) {

    private val sessions = mutableListOf<DefaultWebSocketServerSession>()

    private val moshi = Moshi.Builder().build()

    fun start() {
        embeddedServer(Netty, 8181) {
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
            request == "GetActions" -> {
                onActionsRequested(session)
            }
            request.startsWith("AddAction:") -> {
                val newActionJson = request.substring(10)
                onNewAction(newActionJson)
            }
            request.startsWith("Command:") -> {
                val command = request.substring(8)
                onCommand(command)
            }
            else -> {
                logger.debug("Someone requested something weird: $request")
                session.send(Frame.Text("Unknown request"))
            }
        }
    }

    private suspend fun onActionsRequested(session: DefaultWebSocketServerSession) {
        logger.debug("Someone requested actions")
        val json = getActionsJson()
        session.send(Frame.Text(json))
    }

    private suspend fun onNewAction(newActionJson: String) {
        logger.debug("Someone requested the adding of action: $newActionJson")
        val adapter = moshi.adapter(RemoteAction::class.java)
        val action = adapter.fromJson(newActionJson)!!
        store.addAction(action)

        val actionsJson = getActionsJson()
        dispatch { otherSession ->
            otherSession.send(Frame.Text("Actions:$actionsJson"))
        }
    }

    private fun onCommand(command: String) {
        // TODO Envoyer la commande dans le pipeline
        println("Command received from UI: $command")
    }

    private fun getActionsJson(): String {
        val adapter = moshi.adapter(List::class.java)
        return adapter.toJson(store.getActions())!!
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
