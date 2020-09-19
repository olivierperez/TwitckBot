package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.Deadline
import fr.o80.twitck.lib.api.bean.Importance
import fr.o80.twitck.lib.api.bean.SendMessage
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class MessengerImplTest {

    lateinit var messenger: MessengerImpl

    private val bot: TwitckBot = mockk(relaxed = true)

    @BeforeTest
    fun setup() {
        messenger = MessengerImpl(
            bot = bot,
            intervalBetweenPostponed = Duration.ofMillis(250),
            intervalBetweenRepeatedMessages = Duration.ofMillis(250)
        )
    }

    @AfterTest
    fun tearDown() {
        messenger.interrupt()
    }

    @Test
    fun shouldSendImmediateMessages() {
        messenger.send(SendMessage("chan", "Message 1", Deadline.Immediate))
        messenger.send(SendMessage("chan", "Message 2", Deadline.Immediate))

        verify { bot.send("chan", "Message 1") }
        verify { bot.send("chan", "Message 2") }
    }

    @Test
    fun `should wait before sending HIGH importance postponed messages`() {
        messenger.send(SendMessage("chan", "Message 1", Deadline.Postponed(Importance.HIGH)))
        messenger.send(SendMessage("chan", "Message 2", Deadline.Postponed(Importance.HIGH)))

        Thread.sleep(20)
        verify(exactly = 1) { bot.send("chan", any()) }
    }

    @Test
    fun `should wait before sending LOW importance postponed messages`() {
        messenger.send(SendMessage("chan", "Message 1", Deadline.Postponed(Importance.LOW)))
        messenger.send(SendMessage("chan", "Message 2", Deadline.Postponed(Importance.LOW)))

        Thread.sleep(20)
        verify(exactly = 1) { bot.send("chan", any()) }
    }

    @Test
    fun `should wait before sending postponed messages`() {
        messenger.send(SendMessage("chan", "Message 1", Deadline.Postponed(Importance.HIGH)))
        messenger.send(SendMessage("chan", "Message 2", Deadline.Postponed(Importance.LOW)))

        Thread.sleep(20)
        verify { bot.send("chan", "Message 1") }
        Thread.sleep(500 + 20)
        verify { bot.send("chan", "Message 2") }
    }

    @Test
    fun `should send HIGH importance message before LOW importance ones`() {
        messenger.send(SendMessage("chan", "HIGH 1", Deadline.Postponed(Importance.HIGH)))
        messenger.send(SendMessage("chan", "LOW 1", Deadline.Postponed(Importance.LOW)))
        messenger.send(SendMessage("chan", "LOW 2", Deadline.Postponed(Importance.LOW)))
        messenger.send(SendMessage("chan", "HIGH 2", Deadline.Postponed(Importance.HIGH)))

        Thread.sleep(20)
        verify(exactly = 1) { bot.send("chan", "HIGH 1") }
        Thread.sleep(250 + 20)
        verify(exactly = 1) { bot.send("chan", "HIGH 2") }
    }

    @Test
    fun `should remember cool downs`() {
        val coolDown = CoolDown(Duration.ofMillis(1000))
        val message = SendMessage("chan", "Le marché c'est ça", Deadline.Immediate, coolDown)
        messenger.startCoolDown(message)

        assertTrue(messenger.isCoolingDown(message))
        Thread.sleep(2000)
        assertFalse(messenger.isCoolingDown(message))
    }

    @Test
    fun `should wait for cool down`() {
        val coolDown = CoolDown(Duration.ofMillis(1000))
        val message = SendMessage("chan", "Le marché c'est ça", Deadline.Immediate, coolDown)
        messenger.send(message)
        messenger.send(message)

        verify(exactly = 1) { bot.send(any(), any()) }
    }

    @Test
    fun `should repeat messages`() {
        messenger.send(SendMessage("chan", "Message répété", Deadline.Repeated))
        Thread.sleep(600)
        verify(exactly = 2) { bot.send("chan", "Message répété") }
    }
}