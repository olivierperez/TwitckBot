package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.overlay.graphics.ext.draw
import org.lwjgl.opengl.GL46
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

class ImageRenderer(
    private val height: Int,
    private val width: Int,
    private val textRenderer: TextRenderer = TextRenderer(
        "fonts/Roboto-Black.ttf",
        margin = 0f,
        fontHeight = 70f
    )
) : Renderer {

    private var image: Image? = null
    private var text: String? = null
    private var disappearAt: Instant? = null

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
            text?.let { txt -> renderText(txt, img) }
        }
    }

    private fun renderText(text: String, image: Image) {
        val (left, top, lines, biggestWidth) = computeBestBounds(text, image)

        draw {
            pushed {
                color(0f, 0f, 0f)
                translate(left, top, 0f)
                lines.forEach { line ->
                    pushed {
                        translate((biggestWidth - line.width) / 2f, 0f, 0f)
                        textRenderer.render(line.content)
                    }
                    translate(0f, textRenderer.fontHeight, 0f)
                }
            }
        }
    }

    private fun computeBestBounds(text: String, image: Image): Bounds {
        val oneLineWidth = textRenderer.getStringWidth(text)
        val neededLines = ceil(oneLineWidth / (width - 100)).toInt()
        val charsPerLine = text.length / neededLines

        val lines = LineSplitter().split(text, charsPerLine)
            .map { Line(it, textRenderer.getStringWidth(it)) }
        val biggestLine: Line = lines
            .maxByOrNull { it.width }
            ?: Line("", 0f)

        val left = (width - biggestLine.width - 30) / 2f
        val top = (height + image.height) / 2f

        return Bounds(left, top, lines, biggestLine.width)
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
