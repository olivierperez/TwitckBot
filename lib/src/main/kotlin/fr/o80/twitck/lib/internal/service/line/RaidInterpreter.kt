package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.Badge
import fr.o80.twitck.lib.api.bean.Viewer
import fr.o80.twitck.lib.api.bean.event.RaidEvent
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.internal.handler.RaidDispatcher

class RaidInterpreter(
    private val messenger: Messenger,
    private val raidDispatcher: RaidDispatcher,
    private val logger: Logger
) : LineInterpreter {

    private val userNoticeRegex = "^@([^ ]+) :tmi\\.twitch\\.tv USERNOTICE (#.+)$".toRegex()

    override fun handle(line: String) {
        userNoticeRegex.find(line)?.let { matchResult ->
            val rawTags = matchResult.groupValues[1]
            val channel = matchResult.groupValues[2]

            val tags = rawTags.split(";").map {
                val (key, value) = it.split("=")
                Pair(key, value)
            }.toMap()

            if (tags.msgId == "raid") {
                val viewer = Viewer(
                    login = tags.msgLogin,
                    displayName = tags.msgDisplayName,
                    badges = tags.badges,
                    userId = tags.userId,
                    color = tags.color
                )

                dispatchRaid(channel, tags, viewer)
            }
        }
    }

    private fun dispatchRaid(channel: String, tags: Map<String, String>, viewer: Viewer) {
        val raidEvent = RaidEvent(
            messenger,
            channel,
            viewer,
            tags.msgLogin,
            tags.msgDisplayName,
            tags.msgViewerCount
        )
        logger.error("Raid => $raidEvent")
        raidDispatcher.dispatch(
            raidEvent
        )
    }

    private val Map<String, String>.badges: List<Badge>
        get() = parseBadges(getValue("badges"))

    private val Map<String, String>.color: String
        get() = getValue("color")

    private val Map<String, String>.displayName: String
        get() = getValue("display-name")

    private val Map<String, String>.userId: String
        get() = getValue("user-id")

    private val Map<String, String>.msgId: String?
        get() = get("msg-id")

    private val Map<String, String>.msgDisplayName: String
        get() = getValue("msg-param-displayName")

    private val Map<String, String>.msgLogin: String
        get() = getValue("msg-param-login")

    private val Map<String, String>.msgViewerCount: String
        get() = getValue("msg-param-viewerCount")
}
