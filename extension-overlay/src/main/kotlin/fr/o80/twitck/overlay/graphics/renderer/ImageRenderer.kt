package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.overlay.graphics.ext.draw
import org.lwjgl.opengl.GL46
import java.time.Duration

class ImageRenderer(
    private val height: Int,
    private val width: Int
) : Renderer {

    private var image: Image? = null

    override fun init() {
    }

    override fun tick() {
    }

    override fun render() {
        image?.let { img ->
            img.load()
            renderImage(img)
        }
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

    fun setImage(path: String, duration: Duration) {
        image = Image(path)
    }

}
