package fr.o80.twitck.extension.rewards

import com.squareup.moshi.JsonClass
import fr.o80.twitck.lib.api.bean.ChannelName

@JsonClass(generateAdapter = true)
class RewardsConfiguration(
    val channel: ChannelName,
    val claim: RewardsClaim,
    val talk: RewardsTalk,
    val i18n: RewardsI18n
)

@JsonClass(generateAdapter = true)
class RewardsClaim(
    val command: String = "!claim",
    val reward: Int,
    val secondsBetweenTwoClaims: Long,
    val image: String,
    val positiveSound: String = "positive",
    val negativeSound: String = "negative"
)

@JsonClass(generateAdapter = true)
class RewardsTalk(
    val reward: Int,
    val secondsBetweenTwoTalkRewards: Long
)

@JsonClass(generateAdapter = true)
class RewardsI18n(
    val viewerJustClaimed: String
)
