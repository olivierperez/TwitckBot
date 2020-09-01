package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.ext.draw

class InformationRenderer(
    private val width: Int,
    private val height: Int,
    private val bgColor: Vertex3f,
    private val borderColor: Vertex3f,
    private val textColor: Vertex3f,
    private val textRenderer: TextRenderer = TextRenderer("fonts/Roboto-Light.ttf")
) : Renderer {

    private var texts: List<String> = emptyList()

    override fun init() {
        textRenderer.init()
    }

    override fun render() {
        draw {
            clear(bgColor.x, bgColor.y, bgColor.z)
            lineWidth(1f)

            color(borderColor.x, borderColor.y, borderColor.z)
            rect(0f, 0f, width.toFloat(), height.toFloat())

            color(textColor.x, textColor.y, textColor.z)

            textRenderer.render(texts.joinToString("\n"))
        }
    }

    fun updateTexts(texts: List<String>) {
        this.texts = texts
    }

}