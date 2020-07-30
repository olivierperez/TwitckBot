package fr.o80.twitck.lib.service.line

import fr.o80.twitck.lib.bean.Badge

interface LineHandler {
    fun handle(line: String)

    fun parseBadges(value: String): List<Badge> {
        return value.split(",").map {
            val badgeValue = it.split("/")[0]
            Badge.fromValue(badgeValue)
        }
    }
}