package fr.o80.twitck.lib.api.bean

data class Command(
    val tag: String,
    val options: List<String> = emptyList()
)
