package fr.o80.twitck.poll

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CurrentPollTest {

    @Test
    fun `should compute 2 best results`() {
        // Given
        val poll = CurrentPoll("Titre")
        poll.addVote(Random.nextInt().toString(), "oui")
        poll.addVote(Random.nextInt().toString(), "oui")
        poll.addVote(Random.nextInt().toString(), "oui")

        poll.addVote(Random.nextInt().toString(), "non")
        poll.addVote(Random.nextInt().toString(), "non")

        poll.addVote(Random.nextInt().toString(), "ça dépend")

        poll.addVote(Random.nextInt().toString(), "peut-être")

        // When
        val results = poll.getBestResults(2)

        // Assert
        assertEquals(2, results.size)
        assertEquals(Pair("oui", 3), results[0])
        assertEquals(Pair("non", 2), results[1])
    }

}
