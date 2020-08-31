package fr.o80.twitck.overlay

fun main() {
    val width = 512
    val height = 110
    val overlay = TwitckOverlay("Streaming Overlay", width, height)
    val thread = Thread(overlay)
    thread.start()

    println("Waiting on ${Thread.currentThread().name}")

    overlay.registerRender(DemoRenderer(width, height))
    Thread.sleep(10 * 60 * 1000)
    println("Sort ! Prends la clé !")
    overlay.kill()
}

