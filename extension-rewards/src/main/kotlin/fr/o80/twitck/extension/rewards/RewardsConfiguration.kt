package fr.o80.twitck.extension.rewards

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RewardsConfiguration(
    val channel: String,
    val secondsBetweenTwoClaims: Long,
    val claimedPoints: Int,
    val secondsBetweenTwoTalkRewards: Long,
    val rewardedPoints: Int,
    val i18n: RewardsI18n
)

@JsonClass(generateAdapter = true)
class RewardsI18n(
    val viewerJustClaimed: String
)
