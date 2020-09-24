package fr.o80.twitck.poll

class CurrentPoll(
    val title: String
) {

    private val votes: MutableMap<String, String> = mutableMapOf()

    fun getBestResults(count: Int): List<Pair<String, Int>> =
        votes.values
            .groupBy { it }
            .map { Pair(it.key, it.value.size) }
            .take(count)

    fun addVote(login: String, vote: String): Vote {
        return if (votes.containsKey(login)) {
            votes[login] = vote
            Vote.VOTE_CHANGED
        } else {
            votes[login] = vote
            Vote.NEW_VOTE
        }
    }
}

enum class Vote {
    NEW_VOTE,
    VOTE_CHANGED
}
