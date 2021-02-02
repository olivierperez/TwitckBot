package fr.o80.twitck.poll

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.Badge

@JsonClass(generateAdapter = true)
class PollConfiguration(
    val channel: String,
    val privilegedBadges: List<Badge>,
    val pointsEarnPerVote: Int,
    val i18n: PollI18n
)

@JsonClass(generateAdapter = true)
class PollI18n(
    val errorCreationPollUsage: String,
    val errorDurationIsMissing: String,
    val newPoll: String,
    val pollHasJustFinished: String,
    val currentPollResult: String,
    val oneResultFormat: String,
    val pollHasNoVotes: String
)
