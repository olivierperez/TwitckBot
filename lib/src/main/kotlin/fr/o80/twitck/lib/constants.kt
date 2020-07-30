package fr.o80.twitck.lib

// Server constants
const val HOST = "irc.twitch.tv"
const val PORT = 6667

// Server messages
const val SERVER_MEMANS = ":tmi.twitch.tv CAP * ACK :twitch.tv/membership"
const val SERVER_MEMREQ = "CAP REQ :twitch.tv/membership"
const val SERVER_CMDREQ = "CAP REQ :twitch.tv/commands"
const val SERVER_CMDANS = ":tmi.twitch.tv CAP * ACK :twitch.tv/commands"
const val SERVER_TAGREG = "CAP REQ :twitch.tv/tags"
const val SERVER_TAGANS = ":tmi.twitch.tv CAP * ACK :twitch.tv/tags"