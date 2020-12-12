package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Badge

internal interface LineInterpreter {
    fun handle(line: String)

    fun parseBadges(value: String): List<Badge> {
        return value.split(",").mapNotNull {
            val badgeValue = it.split("/")[0]

            if (badgeValue.isBlank()) {
                null
            } else {
                Badge.fromValue(badgeValue)
            }
        }
    }
}