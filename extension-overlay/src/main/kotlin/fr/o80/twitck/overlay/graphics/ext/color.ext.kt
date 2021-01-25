package fr.o80.twitck.overlay.graphics.ext

fun Int.alpha() = (this shr 24 and 255).toByte()
fun Int.red() = (this shr 16 and 255).toByte()
fun Int.green() = (this shr 8 and 255).toByte()
fun Int.blue() = (this shr 0 and 255).toByte()
