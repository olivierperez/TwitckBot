package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.CoolDown
import fr.o80.twitck.lib.api.bean.Importance
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
            intervalBetweenPostponed = Duration.ofMillis(250)
        )
    }

    @AfterTest
    fun tearDown() {
        messenger.interrupt()
    }

    @Test
    fun shouldSendImmediateMessages() {
        messenger.sendImmediately("chan", "Message 1")
        messenger.sendImmediately("chan", "Message 2")

        verify { bot.send("chan", "Message 1") }
        verify { bot.send("chan", "Message 2") }
    }

    @Test
    fun `should wait before sending HIGH importance postponed messages`() {
        messenger.sendWhenAvailable("chan", "Message 1", Importance.HIGH)
        messenger.sendWhenAvailable("chan", "Message 2", Importance.HIGH)

        Thread.sleep(20)
        verify(exactly = 1) { bot.send("chan", any()) }
    }

    @Test
    fun `should wait before sending LOW importance postponed messages`() {
        messenger.sendWhenAvailable("chan", "Message 1", Importance.LOW)
        messenger.sendWhenAvailable("chan", "Message 2", Importance.LOW)

        Thread.sleep(20)
        verify(exactly = 1) { bot.send("chan", any()) }
    }

    @Test
    fun `should wait before sending postponed messages`() {
        messenger.sendWhenAvailable("chan", "Message 1", Importance.HIGH)
        messenger.sendWhenAvailable("chan", "Message 2", Importance.LOW)

        Thread.sleep(20)
        verify { bot.send("chan", "Message 1") }
        Thread.sleep(500 + 20)
        verify { bot.send("chan", "Message 2") }
    }

    @Test
    fun `should send HIGH importance message before LOW importance ones`() {
        messenger.sendWhenAvailable("chan", "HIGH 1", Importance.HIGH)
        messenger.sendWhenAvailable("chan", "LOW 1", Importance.LOW)
        messenger.sendWhenAvailable("chan", "LOW 2", Importance.LOW)
        messenger.sendWhenAvailable("chan", "HIGH 2", Importance.HIGH)

        Thread.sleep(20)
        verify(exactly = 1) { bot.send("chan", "HIGH 1") }
        Thread.sleep(250 + 20)
        verify(exactly = 1) { bot.send("chan", "HIGH 2") }
    }

    @Test
    fun `should remember cool downs`() {
        val coolDown = CoolDown(Duration.ofMillis(1000))
        val message = "Le marché c'est ça"
        messenger.startCoolDown(message, coolDown)

        assertTrue(messenger.isCoolingDown(message))
        Thread.sleep(2000)
        assertFalse(messenger.isCoolingDown(message))
    }

    @Test
    fun `should wait for cool down`() {
        val coolDown = CoolDown(Duration.ofMillis(1000))
        messenger.sendImmediately("chan", "Le marché c'est ça", coolDown)
        messenger.sendImmediately("chan", "Le marché c'est ça", coolDown)

        verify(exactly = 1) { bot.send(any(), any()) }
    }

}
