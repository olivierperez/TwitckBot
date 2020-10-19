package fr.o80.twitck.lib.api.bean

data class Viewer(
    val login: String,
    var displayName: String,
    var badges : List<Badge>,
    var userId : String,
    var color : String
)
