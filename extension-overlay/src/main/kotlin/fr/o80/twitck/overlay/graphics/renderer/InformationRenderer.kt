package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.ext.draw
import java.time.Duration
import java.time.Instant

class InformationRenderer(
    private val height: Int,
    private val width: Int,
    private val bgColor: Vertex3f,
    private val borderColor: Vertex3f,
    private val textColor: Vertex3f,
    private val textRenderer: TextRenderer = TextRenderer("fonts/Roboto-Light.ttf")
) : Renderer {


    private var informationText: String? = null

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
        val message = alert?.text ?: informationText
        message?.let { text -> renderMessage(text) }
    }

    private fun renderMessage(text: String) {
        draw {
            pushed {
                val messageWidth = textRenderer.getStringWidth(text)
                val messageHeight = textRenderer.getStringHeight()
                val messageBoxWidth = messageWidth + 30
                val messageBoxHeight = messageHeight + 40

                translate(
                    (width - messageBoxWidth) / 2f,
                    height.toFloat() - messageBoxHeight,
                    0f
                )

                color(bgColor.x, bgColor.y, bgColor.z)
                quad(0f, 0f, messageBoxWidth, messageBoxHeight)

                lineWidth(2f)
                color(borderColor.x, borderColor.y, borderColor.z)
                rect(0f, 0f, messageBoxWidth, messageBoxHeight)

                color(textColor.x, textColor.y, textColor.z)

                textRenderer.render(text)
            }
        }
    }

    fun setText(text: String?) {
        this.informationText = text
    }

    fun popAlert(text: String, duration: Duration) {
        this.alert = Alert(text, Instant.now() + duration)
    }

}

data class Alert(
    val text: String,
    val endAt: Instant
)
