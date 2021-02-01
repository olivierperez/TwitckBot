package fr.o80.twitck.poll

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class PollCommandsTest {

    @MockK
    lateinit var i18n: PollI18n

    private lateinit var pollCommands: PollCommands

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { i18n.pollHasNoVotes } returns "pas de votes pour #TITLE#"
        every { i18n.oneResultFormat } returns "[#ANSWER#|#COUNT#]"

        pollCommands = PollCommands(
            channel = "channel",
            privilegedBadges = listOf(),
            i18n = i18n,
            pointsForEachVote = 5,
            extensionProvider = mockk()
        )
    }

    @Test
    fun `should generate message for empty poll`() {
        val poll = CurrentPoll("titre")

        val resultMessage = pollCommands.generateResultMessage(poll, "peu importe")

        assertEquals("pas de votes pour titre", resultMessage)
    }

    @Test
    fun `should generate message for non-empty poll`() {
        val poll = CurrentPoll("titre")
        poll.addVote("1", "oui")
        poll.addVote("2", "oui")
        poll.addVote("3", "non")

        val resultMessage = pollCommands.generateResultMessage(poll, "--#TITLE#--#RESULTS#--")

        assertEquals("--titre--[oui|2], [non|1]--", resultMessage)
    }

}