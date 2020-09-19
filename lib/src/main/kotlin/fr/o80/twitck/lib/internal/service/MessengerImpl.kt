package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.Deadline
import fr.o80.twitck.lib.api.bean.SendMessage
import fr.o80.twitck.lib.api.service.Messenger
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.PriorityBlockingQueue

class MessengerImpl(
    private val bot: TwitckBot,
    private val intervalBetweenPostponed: Duration = Duration.ofSeconds(30),
    private val intervalBetweenRepeatedMessages: Duration = Duration.ofMinutes(5),
) : Messenger {

    private val messagesToSend: PriorityBlockingQueue<SendMessage> = PriorityBlockingQueue(11, SendMessageComparator)

    private var interrupted: Boolean = false

    private val coolDowns: MutableMap<String, LocalDateTime> = mutableMapOf()

    private val repeatedMessages: MutableList<SendMessage> = mutableListOf()

    init {
        Thread {
            while (!interrupted) {
                messagesToSend.take().let { message ->
                    bot.send(message.channel, message.content)
                }
                Thread.sleep(intervalBetweenPostponed.toMillis())
            }
        }.start()
        Thread {
            while (!interrupted) {
                Thread.sleep(intervalBetweenRepeatedMessages.toMillis())
                repeatedMessages.randomOrNull()?.let { message ->
                    bot.send(message.channel, message.content)
                }
            }
        }.start()
    }

    override fun send(message: SendMessage) {
        if (isCoolingDown(message)) return
        startCoolDown(message)

        when (message.deadline) {
            Deadline.Immediate -> bot.send(message.channel, message.content)
            Deadline.Repeated -> recordRepeated(message)
            else -> messagesToSend.offer(message)
        }
    }

    fun interrupt() {
        interrupted = true
    }

    internal fun startCoolDown(message: SendMessage) {
        message.coolDown?.duration?.let { duration ->
            coolDowns[message.content] = LocalDateTime.now() + duration
        }
    }

    internal fun isCoolingDown(message: SendMessage): Boolean {
        val expiry = coolDowns[message.content]
        return expiry != null && LocalDateTime.now().isBefore(expiry)
    }

    private fun recordRepeated(message: SendMessage) {
        repeatedMessages += message
    }

}
