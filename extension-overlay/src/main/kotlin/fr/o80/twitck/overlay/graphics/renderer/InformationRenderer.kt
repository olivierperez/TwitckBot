package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.ext.draw
import java.time.Duration
import java.time.Instant

class InformationRenderer(
    private val width: Int,
    private val height: Int,
    private val bgColor: Vertex3f,
    private val borderColor: Vertex3f,
    private val textColor: Vertex3f,
    private val textRenderer: TextRenderer = TextRenderer("fonts/Roboto-Light.ttf")
) : Renderer {

    private var texts: List<String> = emptyList()

    private var alert: Alert? = null

    override fun init() {
        textRenderer.init()
    }

    override fun tick() {
        if (alert?.endAt?.isBefore(Instant.now()) == true) {
            alert = null
        }
    }

    override fun render() {
        draw {
            clear(bgColor.x, bgColor.y, bgColor.z)
            lineWidth(1f)

            color(borderColor.x, borderColor.y, borderColor.z)
            rect(0f, 0f, width.toFloat(), height.toFloat())

            color(textColor.x, textColor.y, textColor.z)

            with(alert) {
                if (this != null) {
                    textRenderer.render(this.text)
                } else {
                    textRenderer.render(texts.joinToString("\n"))
                }
            }
        }
    }

    fun updateTexts(texts: List<String>) {
        this.texts = texts
    }

    fun popAlert(text: String, duration: Duration) {
        this.alert = Alert(text, Instant.now() + duration)
    }

}

data class Alert(
    val text: String,
    val endAt: Instant
)