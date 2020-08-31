package fr.o80.twitck.overlay

fun main() {
    val width = 500
    val height = 150
    val overlay = TwitckOverlay("Streaming Overlay", width,height)
    val thread = Thread(overlay)
    thread.start()

    println("Waiting on ${Thread.currentThread().name}")

    overlay.registerRender(DemoRenderer(height, width))
    Thread.sleep(10 * 60 * 1000)
    println("Sort ! Prends la clé !")
    overlay.kill()
}

