package fr.o80.twitck.overlay

import fr.o80.twitck.overlay.IOUtil.ioResourceToByteBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.GL_ALPHA
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_FILL
import org.lwjgl.opengl.GL11.GL_FRONT
import org.lwjgl.opengl.GL11.GL_LINE
import org.lwjgl.opengl.GL11.GL_LINEAR
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11.glBegin
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glClearColor
import org.lwjgl.opengl.GL11.glColor3f
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glEnd
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glPolygonMode
import org.lwjgl.opengl.GL11.glTexCoord2f
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL11.glVertex2f
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap
import org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance
import org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics
import org.lwjgl.stb.STBTruetype.stbtt_InitFont
import org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight
import org.lwjgl.system.MemoryStack.stackPush
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.math.round


class DemoRenderer(
    private val width: Int,
    private val height: Int
) : Renderer {

    private val texts = listOf(
        "Vous connaissez la commande !claim ?",
        "Ou !points_info ?",
        "Et !discord ? et !github ? ou !help"
    )

    private val text get() =
        texts.joinToString("\n")

    private val margin: Float = 10f
    private val fontHeight: Float = 25f
    private val kerningEnabled = true
    private val drawBoxBorder = false
    private val fontPath = "fonts/Roboto-Light.ttf"

    private var ttf: ByteBuffer? = null
    private var fontInfo: STBTTFontinfo? = null

    private var ascent = 0
    private var descent = 0
    private var lineGap = 0

    private var bitmapWidth: Int = 0
    private var bitmapHeight: Int = 0
    private var charData: STBTTBakedChar.Buffer? = null

    override fun init() {
        try {
            ttf = ioResourceToByteBuffer(fontPath, 512 * 1024)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, ttf)) {
            throw IllegalStateException("Failed to initialize font information.")
        }

        stackPush().use { stack ->
            val pAscent = stack.mallocInt(1)
            val pDescent = stack.mallocInt(1)
            val pLineGap = stack.mallocInt(1)
            stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, pLineGap)
            ascent = pAscent[0]
            descent = pDescent[0]
            lineGap = pLineGap[0]
        }

        initFont()
    }

    override fun render() {
        draw {
            clear(0.8f, 0.8f, 0.8f)

            color(0f, 0f, 0f)
            rect(1f, 1f, width.toFloat(), height.toFloat())

            color(20 / 255f, 20 / 255f, 20 / 255f)

            pushed {
                translate(margin, margin + fontHeight, 0f)
                renderText()
            }
        }
    }

    private fun initFont() {
        bitmapWidth = round(512 * getContentScaleX()).toInt()
        bitmapHeight = round(512 * getContentScaleY()).toInt()

        charData = STBTTBakedChar.malloc(96)

        val texID: Int = glGenTextures()
        val bitmap = BufferUtils.createByteBuffer(bitmapWidth * bitmapHeight)

        stbtt_BakeFontBitmap(ttf, fontHeight * getContentScaleY(), bitmap, bitmapWidth, bitmapHeight, 32, charData)

        glBindTexture(GL_TEXTURE_2D, texID)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, bitmapWidth, bitmapHeight, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f) // BG color
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f) // Text color
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    }

    private fun getContentScaleX(): Float = 1.0f
    private fun getContentScaleY(): Float = 1.0f

    private fun renderText() {
        val scale = stbtt_ScaleForPixelHeight(fontInfo, fontHeight)

        stackPush().use { stack ->
            val pCodePoints = stack.mallocInt(1)
            val x = stack.floats(0.0f)
            val y = stack.floats(0.0f)
            val q = STBTTAlignedQuad.mallocStack(stack)
            var lineStart = 0
            val factorX = 1.0f / getContentScaleX()
            val factorY = 1.0f / getContentScaleY()
            var lineY = 0.0f
            glBegin(GL_QUADS)
            var i = 0
            val to: Int = text.length
            while (i < to) {
                i += getCodePoint(text, to, i, pCodePoints)
                val codePoint = pCodePoints[0]
                if (codePoint == '\n'.toInt()) {
                    if (drawBoxBorder) {
                        glEnd()
                        renderLineBB(lineStart, i - 1, y[0], scale)
                        glBegin(GL_QUADS)
                    }
                    y.put(0, y[0] + (ascent - descent + lineGap) * scale.also { lineY = it })
                    x.put(0, 0.0f)
                    lineStart = i
                    continue
                } else if (codePoint < 32 || 128 <= codePoint) {
                    continue
                }
                val cpX = x[0]
                stbtt_GetBakedQuad(charData, bitmapWidth, bitmapHeight, codePoint - 32, x, y, q, true)
                x.put(0, scale(cpX, x[0], factorX))
                if (kerningEnabled && i < to) {
                    getCodePoint(text, to, i, pCodePoints)
                    x.put(0, x[0] + stbtt_GetCodepointKernAdvance(fontInfo, codePoint, pCodePoints[0]) * scale)
                }
                val x0: Float = scale(cpX, q.x0(), factorX)
                val x1: Float = scale(cpX, q.x1(), factorX)
                val y0: Float = scale(lineY, q.y0(), factorY)
                val y1: Float = scale(lineY, q.y1(), factorY)
                glTexCoord2f(q.s0(), q.t0())
                glVertex2f(x0, y0)
                glTexCoord2f(q.s1(), q.t0())
                glVertex2f(x1, y0)
                glTexCoord2f(q.s1(), q.t1())
                glVertex2f(x1, y1)
                glTexCoord2f(q.s0(), q.t1())
                glVertex2f(x0, y1)
            }
            glEnd()
            if (drawBoxBorder) {
                renderLineBB(lineStart, text.length, lineY, scale)
            }
        }
    }

    private fun scale(center: Float, offset: Float, factor: Float): Float {
        return (offset - center) * factor + center
    }

    private fun renderLineBB(from: Int, to: Int, y: Float, scale: Float) {
        val info = fontInfo ?: throw IllegalStateException("This class is not initialized")
        val adjustedY = y - descent * scale
        glDisable(GL_TEXTURE_2D)
        glPolygonMode(GL_FRONT, GL_LINE)
        glColor3f(1.0f, 1.0f, 0.0f)
        val width = getStringWidth(info, text, from, to)
        glBegin(GL_QUADS)
        glVertex2f(0.0f, adjustedY)
        glVertex2f(width, adjustedY)
        glVertex2f(width, adjustedY - fontHeight)
        glVertex2f(0.0f, adjustedY - fontHeight)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glPolygonMode(GL_FRONT, GL_FILL)
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f) // Text color
    }

    private fun getStringWidth(info: STBTTFontinfo, text: String, from: Int, to: Int): Float {
        var width = 0
        stackPush().use { stack ->
            val pCodePoint = stack.mallocInt(1)
            val pAdvancedWidth = stack.mallocInt(1)
            val pLeftSideBearing = stack.mallocInt(1)
            var i = from
            while (i < to) {
                i += getCodePoint(text, to, i, pCodePoint)
                val cp = pCodePoint[0]
                stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing)
                width += pAdvancedWidth[0]
                if (kerningEnabled && i < to) {
                    getCodePoint(text, to, i, pCodePoint)
                    width += stbtt_GetCodepointKernAdvance(info, cp, pCodePoint[0])
                }
            }
        }
        return width * stbtt_ScaleForPixelHeight(info, fontHeight)
    }

    private fun getCodePoint(text: String, to: Int, i: Int, cpOut: IntBuffer): Int {
        val c1 = text[i]
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            val c2 = text[i + 1]
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2))
                return 2
            }
        }
        cpOut.put(0, c1.toInt())
        return 1
    }

}