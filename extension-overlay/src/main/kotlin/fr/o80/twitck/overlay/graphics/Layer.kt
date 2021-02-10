package fr.o80.twitck.overlay.graphics

interface Layer {
    fun init()
    fun tick()
    fun render()
}
