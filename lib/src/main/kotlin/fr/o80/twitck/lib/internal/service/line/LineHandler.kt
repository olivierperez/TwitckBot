package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Badge

internal interface LineHandler {
    fun handle(line: String)

    fun parseBadges(value: String): List<Badge> {
        return value.split(",").map {
            val badgeValue = it.split("/")[0]
            Badge.fromValue(badgeValue)
        }
    }
}