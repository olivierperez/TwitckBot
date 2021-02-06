package fr.o80.twitck.lib.api.bean

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ViewerTest {

    @Test
    fun shouldUseHisPrivileges() {
        val viewer = Viewer(
            login = "login",
            displayName = "LoGiN",
            badges = listOf(Badge.BITS_LEADER, Badge.BITS),
            userId = "AZE123",
            color = "transparent"
        )

        val hasNoPrivileges = viewer hasNoPrivilegesOf listOf(Badge.BITS_LEADER, Badge.BROADCASTER)

        assertFalse(
            hasNoPrivileges,
            "Viewer has the privilege BIT_LEADER he should succeed the check"
        )
    }

    @Test
    fun shouldFailToCheckPrivileges() {
        val viewer = Viewer(
            login = "login",
            displayName = "LoGiN",
            badges = listOf(Badge.BITS_LEADER, Badge.BITS),
            userId = "AZE123",
            color = "transparent"
        )

        val hasNoPrivileges = viewer hasNoPrivilegesOf listOf(Badge.FOUNDER, Badge.BROADCASTER)

        assertTrue(hasNoPrivileges, "Viewer has none of the required privileges")
    }

}
