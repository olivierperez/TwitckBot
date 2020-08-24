package fr.o80.twitck.lib.api.bean

data class Command(
    val login: String,
    val badges: List<Badge>,
    val tag: String,
    val options: List<String> = emptyList()
)
