package fr.o80.twitck.lib.internal.service.line

import fr.o80.twitck.lib.api.bean.*
import fr.o80.twitck.lib.api.service.CommandParser
import fr.o80.twitck.lib.api.service.Messenger
import fr.o80.twitck.lib.internal.handler.CommandDispatcher
import fr.o80.twitck.lib.internal.handler.MessageDispatcher
import fr.o80.twitck.lib.internal.handler.RaidDispatcher

internal class PrivMsgLineHandler(
    private val messenger: Messenger,
    private val commandParser: CommandParser,
    private val messageDispatcher: MessageDispatcher,
    private val commandDispatcher: CommandDispatcher,
    private val raidDispatcher: RaidDispatcher
) : LineHandler {

    private val regex =
        Regex("^@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG (#[^ ]+) :(.+)$")

    override fun handle(line: String) {
        regex.find(line)?.let { matchResult ->
            val rawTags = matchResult.groupValues[1]
            val user = matchResult.groupValues[2]
            val channel = matchResult.groupValues[3]
            val msg = matchResult.groupValues[4]

            val tags = rawTags.split(";").map {
                val (key, value) = it.split("=")
                Pair(key, value)
            }.toMap()

            val viewer = Viewer(
                login = user,
                displayName = tags.displayName,
                badges = tags.badges,
                userId = tags.userId,
                color = tags.color
            )

            val command = commandParser.parse(msg)

            when {
                tags.msgId == "raid" -> dispatchRaid(channel, tags, viewer)
                command != null -> dispatchCommand(channel, command, tags, viewer)
                else -> dispatchMessage(channel, msg, tags, viewer)
            }
        }
    }

    private fun dispatchRaid(channel: String, tags: Map<String, String>, viewer: Viewer) {
        raidDispatcher.dispatch(
            RaidEvent(
                messenger,
                channel,
                viewer,
                tags.msgLogin,
                tags.msgDisplayName,
                tags.msgViewerCount
            )
        )
    }

    private fun dispatchCommand(
        channel: String,
        command: Command,
        tags: Map<String, String>,
        viewer: Viewer
    ) {
        commandDispatcher.dispatch(
            CommandEvent(
                messenger,
                channel,
                command,
                tags.bits,
                viewer
            )
        )
    }

    private fun dispatchMessage(
        channel: String,
        msg: String,
        tags: Map<String, String>,
        viewer: Viewer
    ) {
        messageDispatcher.dispatch(
            MessageEvent(
                messenger,
                channel,
                msg,
                tags.bits,
                viewer
            )
        )
    }

    private val Map<String, String>.badges: List<Badge>
        get() = parseBadges(getValue("badges"))

    private val Map<String, String>.color: String
        get() = getValue("color")

    private val Map<String, String>.bits: Int
        get() = get("bits")?.toInt() ?: 0

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
