package fr.o80.twitck.overlay

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY
import org.lwjgl.opengl.GL11.glDisableClientState
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL11.glEnableClientState
import org.lwjgl.opengl.GL11.glVertexPointer
import org.lwjgl.stb.STBEasyFont.stb_easy_font_print


class DemoRenderer(
    private val height: Int,
    private val width: Int
) : Renderer {

    private val texts = listOf(
        "Vous connaissez la commande !claim ?",
        "Ou !points_info ?",
        "Et !discord ? et !github ? ou !help"
    )

    private val margin: Float = 20f
    private val fontHeight: Float = 20f
    private val lineSpacing: Float = 7f
    private val scaleFactor: Float = 2f

    override fun render() {

        draw {
            clear(0.8f, 0.8f, 0.8f)
            color(0f, 0f, 0f)
            rect(1f, 1f, width.toFloat(), height.toFloat())
            color(209 / 255f, 10 / 255f, 73 / 255f)

            pushed {
                // Scroll
                translate(margin, margin, 0f)

                // Zoom
                scale(scaleFactor, scaleFactor, 1f);

                texts.forEach { text ->
                    drawText(text)
                    translate(0f, (fontHeight + lineSpacing) / scaleFactor, 0f)
                }
            }
        }


    }

    private fun drawText(text: String) {
        val charBuffer = BufferUtils.createByteBuffer(text.length * 500)
        val quads = stb_easy_font_print(0f, 0f, text, null, charBuffer)
        glEnableClientState(GL_VERTEX_ARRAY)
        glVertexPointer(2, GL_FLOAT, 16, charBuffer)
        glDrawArrays(GL_QUADS, 0, quads * 4)
        glDisableClientState(GL_VERTEX_ARRAY)
    }

}