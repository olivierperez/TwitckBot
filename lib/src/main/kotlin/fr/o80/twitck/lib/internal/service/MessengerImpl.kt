package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.PostponedMessage
import fr.o80.twitck.lib.api.service.Messenger
import java.time.Duration
import java.util.concurrent.PriorityBlockingQueue

private const val COOL_DOWN_NAMESPACE = "messenger"

class MessengerImpl(
    private val bot: TwitckBot,
    private val coolDownManager: CoolDownManager,
    private val intervalBetweenPostponed: Duration = Duration.ofSeconds(30)
) : Messenger {

    private val messagesToSend: PriorityBlockingQueue<PostponedMessage> = PriorityBlockingQueue(11, PostponedMessageComparator)

    private var interrupted: Boolean = false

    init {
        Thread {
            while (!interrupted) {
                messagesToSend.take().let { message ->
                    bot.send(message.channel, message.content)
                }
                Thread.sleep(intervalBetweenPostponed.toMillis())
            }
        }.start()
    }

    override fun sendImmediately(channel: String, content: String, coolDown: CoolDown?) {
        if (coolDownManager.isCoolingDown(COOL_DOWN_NAMESPACE, content)) return
        coolDownManager.startCoolDown(COOL_DOWN_NAMESPACE, content, coolDown)

        bot.send(channel, content)
    }

    override fun sendWhenAvailable(channel: String, content: String, importance: Importance, coolDown: CoolDown?) {
        if (coolDownManager.isCoolingDown(COOL_DOWN_NAMESPACE, content)) return
        coolDownManager.startCoolDown(COOL_DOWN_NAMESPACE, content, coolDown)

        messagesToSend.offer(PostponedMessage(channel, content, importance))
    }

    override fun sendLine(line: String) {
        bot.sendLine(line)
    }

    override fun whisper(channel: String, recipient: String, message: String) {
        bot.send(channel, "/w $recipient $message")
    }

    fun interrupt() {
        interrupted = true
    }

}
