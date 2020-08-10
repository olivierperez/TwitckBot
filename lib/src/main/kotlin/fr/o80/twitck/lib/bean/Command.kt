package fr.o80.twitck.lib.bean

class Command(
    val badges: List<Badge>,
    val tag: String,
    val options: List<String> = emptyList()
)
