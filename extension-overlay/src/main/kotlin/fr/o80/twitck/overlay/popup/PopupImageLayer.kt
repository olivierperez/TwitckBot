package fr.o80.twitck.overlay.popup

import fr.o80.twitck.overlay.graphics.Layer
import fr.o80.twitck.overlay.graphics.ext.Draw
import fr.o80.twitck.overlay.graphics.ext.Vertex3f
import fr.o80.twitck.overlay.graphics.ext.draw
import fr.o80.twitck.overlay.graphics.model.Image
import fr.o80.twitck.overlay.graphics.renderer.TextRenderer
import fr.o80.twitck.overlay.graphics.utils.LineSplitter
import org.lwjgl.opengl.GL46
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

class PopupImageLayer(
    private val height: Int,
    private val width: Int,
    private val textRenderer: TextRenderer = TextRenderer(
        "fonts/Roboto-Black.ttf",
        fontHeight = 55f
    ),
    private val textColor: Vertex3f,
    private val backgroundColor: Vertex3f,
    private val borderColor: Vertex3f
) : Layer {

    private var image: Image? = null
    private var text: String? = null
    private var disappearAt: Instant? = null

    private val horizontalPadding = 20f
    private val verticalPadding = 10f

    override fun init() {
        textRenderer.init()
    }

    override fun tick() {
        disappearAt?.let { instant ->
            if (instant.isBefore(Instant.now())) {
                disappearAt = null
                image = null
            }
        }
    }

    override fun render() {
        image?.let { img ->
            img.load()
            renderImage(img)
            text?.let { txt -> render(txt, img) }
        }
    }

    private fun render(text: String, image: Image) {
        val (left, top, lines, biggestWidth) = computeBestBounds(text, image)
        val fontHeight = textRenderer.getStringHeight()

        draw {
            pushed {
                translate(left - horizontalPadding, top, 0f)
                drawBackground(biggestWidth, fontHeight, lines.size)
                drawText(biggestWidth, fontHeight, lines)
            }
        }
    }

    private fun Draw.drawBackground(
        biggestWidth: Float,
        fontHeight: Float,
        linesCount: Int
    ) {
        color(backgroundColor)
        quad(
            0f,
            0f,
            biggestWidth + horizontalPadding * 2,
            fontHeight * linesCount + verticalPadding * 2
        )

        lineWidth(2f)
        color(borderColor)
        rect(
            0f,
            0f,
            biggestWidth + horizontalPadding * 2,
            fontHeight * linesCount + verticalPadding * 2
        )
    }

    private fun Draw.drawText(
        biggestWidth: Float,
        fontHeight: Float,
        lines: List<Line>
    ) {
        color(textColor)
        translate(0f, verticalPadding, 0f)
        lines.forEach { line ->
            pushed {
                translate(horizontalPadding + (biggestWidth - line.width) / 2f, 0f, 0f)
                textRenderer.render(line.content)
            }
            translate(0f, fontHeight, 0f)
        }
    }

    private fun computeBestBounds(text: String, image: Image): Bounds {
        val oneLineWidth = textRenderer.getStringWidth(text)
        val neededLines = ceil(oneLineWidth / (width - 100)).toInt()
        val charsPerLine = text.length / neededLines

        val lines = LineSplitter().split(text, charsPerLine)
            .map { Line(it, textRenderer.getStringWidth(it)) }
        val biggestLine: Float = lines
            .maxOfOrNull { it.width }
            ?: 0f

        val left = (width - biggestLine) / 2f
        val top = (height + image.height) / 2f

        return Bounds(left, top, lines, biggestLine)
    }

    private fun renderImage(image: Image) {
        val left = (width - image.width) / 2
        val right = (left + image.width)
        val top = (height - image.height) / 2
        val bottom = (top + image.height)

        draw {
            texture2d {
                color(1f, 1f, 1f)
                GL46.glBindTexture(GL46.GL_TEXTURE_2D, image.id)

                GL46.glBegin(GL46.GL_QUADS)
                GL46.glTexCoord2f(0f, 0f)
                GL46.glVertex2i(left, top)

                GL46.glTexCoord2f(1f, 0f)
                GL46.glVertex2i(right, top)

                GL46.glTexCoord2f(1f, 1f)
                GL46.glVertex2i(right, bottom)

                GL46.glTexCoord2f(0f, 1f)
                GL46.glVertex2i(left, bottom)
                GL46.glEnd()
            }
        }
    }

    fun setImage(inputStream: InputStream, text: String?, duration: Duration) {
        this.image = Image(inputStream)
        this.text = text
        this.disappearAt = Instant.now() + duration
    }

    private data class Bounds(
        val left: Float,
        val top: Float,
        val lines: List<Line>,
        val biggestWidth: Float
    )

    private data class Line(
        val content: String,
        val width: Float
    )

}
