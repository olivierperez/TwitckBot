package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.CoolDown
import org.junit.Test
import java.time.Duration
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CoolDownManagerTest {

    private val coolDownManager = CoolDownManager()

    @Test
    fun `should remember cool downs`() {
        val coolDown = CoolDown(Duration.ofMillis(1000))
        val message = "Le marché c'est ça"
        coolDownManager.startCoolDown(message, coolDown)

        assertTrue(coolDownManager.isCoolingDown(message))
        Thread.sleep(2000)
        assertFalse(coolDownManager.isCoolingDown(message))
    }

}
