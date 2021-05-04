package fr.o80.twitck.lib.internal.service.line

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
            val tags = Tags.from(matchResult.groupValues[1])
            val channel = matchResult.groupValues[2]

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

    private fun dispatchRaid(channel: String, tags: Tags, viewer: Viewer) {
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
}
