package fr.o80.twitck.lib.internal

// Server constants
internal const val HOST = "irc.twitch.tv"
internal const val PORT = 6667

// Server messages
internal const val SERVER_MEMANS = ":tmi.twitch.tv CAP * ACK :twitch.tv/membership"
internal const val SERVER_MEMREQ = "CAP REQ :twitch.tv/membership"
internal const val SERVER_CMDREQ = "CAP REQ :twitch.tv/commands"
internal const val SERVER_CMDANS = ":tmi.twitch.tv CAP * ACK :twitch.tv/commands"
internal const val SERVER_TAGREG = "CAP REQ :twitch.tv/tags"
internal const val SERVER_TAGANS = ":tmi.twitch.tv CAP * ACK :twitch.tv/tags"