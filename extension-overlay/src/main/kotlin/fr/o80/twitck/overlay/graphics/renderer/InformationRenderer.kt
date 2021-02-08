package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.ext.draw
import java.time.Duration
import java.time.Instant

class InformationRenderer(
    private val height: Int,
    private val width: Int,
    private val backgroundColor: Vertex3f,
    private val borderColor: Vertex3f,
    private val textColor: Vertex3f,
    private val textRenderer: TextRenderer = TextRenderer("fonts/Roboto-Light.ttf", fontHeight = 30f)
) : Renderer {

    private var informationText: String? = null

    private var alert: Alert? = null

    private val horizontalPadding = 20f
    private val verticalPadding = 10f

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
                val messageBoxWidth = messageWidth + horizontalPadding * 2
                val messageBoxHeight = messageHeight + verticalPadding * 2

                translate(
                    (width - messageBoxWidth) / 2f,
                    height.toFloat() - messageBoxHeight,
                    0f
                )

                color(backgroundColor)
                quad(0f, 0f, messageBoxWidth, messageBoxHeight)

                lineWidth(2f)
                color(borderColor)
                rect(0f, 0f, messageBoxWidth, messageBoxHeight)

                color(textColor)

                translate(horizontalPadding, verticalPadding, 0f)
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
