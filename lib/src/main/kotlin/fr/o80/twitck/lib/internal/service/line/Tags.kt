package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Badge

fun List<Pair<String, String>>.toTags(): Tags {
    return Tags(this.toMap())
}

class Tags(
    private val data: Map<String, String>
) {

    val badges: List<Badge>
        get() = parseBadges(data.getValue("badges"))

    val bits: Int?
        get() = data["bits"]?.toInt()

    val customRewardId: String?
        get() = data["custom-reward-id"]

    val color: String
        get() = data.getValue("color")

    val displayName: String
        get() = data.getValue("display-name")

    val userId: String
        get() = data.getValue("user-id")

    val msgId: String?
        get() = data["msg-id"]

    val msgDisplayName: String
        get() = data.getValue("msg-param-displayName")

    val msgLogin: String
        get() = data.getValue("msg-param-login")

    val msgViewerCount: String
        get() = data.getValue("msg-param-viewerCount")

    private fun parseBadges(value: String): List<Badge> {
        return value.split(",").mapNotNull {
            val badgeValue = it.split("/")[0]

            if (badgeValue.isBlank()) {
                null
            } else {
                Badge.fromValue(badgeValue)
            }
        }
    }

    companion object {
        fun from(raw: String): Tags {
            return raw.split(";").map {
                val (key, value) = it.split("=")
                Pair(key, value)
            }.toTags()
        }
    }
}
