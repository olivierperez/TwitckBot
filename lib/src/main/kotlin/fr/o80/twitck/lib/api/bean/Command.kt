package fr.o80.twitck.lib.api.bean

class Command(
    val badges: List<Badge>,
    val tag: String,
    val options: List<String> = emptyList()
)
