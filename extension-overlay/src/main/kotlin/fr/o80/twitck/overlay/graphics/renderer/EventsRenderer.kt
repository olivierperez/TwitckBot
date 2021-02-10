package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.lib.api.extension.OverlayEvent
import fr.o80.twitck.overlay.EventsConfiguration
import fr.o80.twitck.overlay.OverlayStyle
import fr.o80.twitck.overlay.graphics.ext.Draw
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.ext.draw
import fr.o80.twitck.overlay.toVertex3f

class EventsRenderer(
    private val style: OverlayStyle,
    private val config: EventsConfiguration,
    private val textRenderer: TextRenderer = TextRenderer(
        "fonts/Roboto-Light.ttf",
        fontHeight = 30f
    )
) : Renderer {

    private var events: List<OverlayEvent> = emptyList()

    override fun init() {
        textRenderer.init()
    }

    override fun tick() {
    }

    override fun render() {
        draw {
            drawBorder()
            drawEvents()
        }
    }

    private fun Draw.drawBorder() {
        color(style.borderColor.toVertex3f())
        rect(
            config.x,
            config.y,
            config.x + config.width,
            config.y + config.height
        )
    }

    fun update(events: List<OverlayEvent>) {
        this.events = events.asReversed()
    }

    private fun Draw.drawEvents() {
        pushed {
            translate(config.x, config.y + config.height, 0f)

            val blockHeight = 45f
            val verticalSpacing = 5f

            val bgColor = style.backgroundColor.toVertex3f()
            val textColor = style.textColor.toVertex3f()

            events.forEach { overlayEvent ->
                translate(0f, -blockHeight, 0f)
                drawBackground(bgColor, blockHeight)
                drawText(textColor, overlayEvent)
                translate(0f, -verticalSpacing, 0f)
            }
        }
    }

    private fun Draw.drawBackground(
        bgColor: Vertex3f,
        blockHeight: Float
    ) {
        color(bgColor)
        quad(0f, 0f, config.width, blockHeight)
    }

    private fun Draw.drawText(
        textColor: Vertex3f,
        overlayEvent: OverlayEvent
    ) {
        pushed {
            translate(10f, 1f, 0f)
            color(textColor)
            textRenderer.render(overlayEvent.text)
        }
    }

}
