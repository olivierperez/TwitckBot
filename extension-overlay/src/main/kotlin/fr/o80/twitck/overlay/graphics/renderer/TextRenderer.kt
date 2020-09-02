package fr.o80.twitck.overlay.graphics.renderer

import fr.o80.twitck.overlay.graphics.IOUtil
import fr.o80.twitck.overlay.graphics.ext.draw
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL46
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.math.round

class TextRenderer(
    private val fontPath: String,
    private val margin: Float = 10f,
    private val fontHeight: Float = 20f
) {

    private val kerningEnabled = true
    private val drawBoxBorder = false

    lateinit var ttf: ByteBuffer
    lateinit var fontInfo: STBTTFontinfo

    private var ascent = 0
    private var descent = 0
    private var lineGap = 0

    private var bitmapWidth: Int = 0
    private var bitmapHeight: Int = 0
    lateinit var charData: STBTTBakedChar.Buffer

    private val contentScaleX: Float = 1.0f
    private val contentScaleY: Float = 1.0f

    fun init() {
        try {
            ttf = IOUtil.ioResourceToByteBuffer(fontPath, 512 * 1024)
        } catch (e: IOException) {
            throw IllegalStateException("Failed to load TTF!")
        }

        fontInfo = STBTTFontinfo.create()
        if (!STBTruetype.stbtt_InitFont(fontInfo, ttf)) {
            throw IllegalStateException("Failed to initialize font information!")
        }

        MemoryStack.stackPush().use { stack ->
            val pAscent = stack.mallocInt(1)
            val pDescent = stack.mallocInt(1)
            val pLineGap = stack.mallocInt(1)
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, pLineGap)
            ascent = pAscent[0]
            descent = pDescent[0]
            lineGap = pLineGap[0]
        }

        bitmapWidth = round(512 * contentScaleX).toInt()
        bitmapHeight = round(512 * contentScaleY).toInt()
        val bitmap = BufferUtils.createByteBuffer(bitmapWidth * bitmapHeight)

        initFont(bitmap, bitmapWidth, bitmapHeight)
        initGl(bitmap)
    }

    fun render(text: String) {
        GL46.glEnable(GL46.GL_TEXTURE_2D)
        GL46.glEnable(GL46.GL_BLEND)
        draw {
            pushed {
                translate(margin, margin + fontHeight, 0f)
                renderText(text)
            }
        }
        GL46.glDisable(GL46.GL_BLEND)
        GL46.glDisable(GL46.GL_TEXTURE_2D)
    }

    private fun initFont(bitmap: ByteBuffer, bitmapWidth: Int, bitmapHeight: Int) {
        charData = STBTTBakedChar.malloc(96)

        STBTruetype.stbtt_BakeFontBitmap(
            ttf,
            fontHeight * contentScaleY,
            bitmap,
            bitmapWidth,
            bitmapHeight,
            32,
            charData
        )
    }

    private fun initGl(bitmap: ByteBuffer) {
        val texID: Int = GL46.glGenTextures()
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, texID)
        GL46.glTexImage2D(
            GL46.GL_TEXTURE_2D,
            0,
            GL46.GL_ALPHA,
            this.bitmapWidth,
            this.bitmapHeight,
            0,
            GL46.GL_ALPHA,
            GL46.GL_UNSIGNED_BYTE,
            bitmap
        )
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR)
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR)
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun renderText(text: String) {
        val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, fontHeight)

        MemoryStack.stackPush().use { stack ->
            val pCodePoints = stack.mallocInt(1)
            val x = stack.floats(0.0f)
            val y = stack.floats(0.0f)
            val q = STBTTAlignedQuad.mallocStack(stack)
            var lineStart = 0
            val factorX = 1.0f / contentScaleX
            val factorY = 1.0f / contentScaleY
            var lineY = 0.0f
            GL46.glBegin(GL46.GL_QUADS)
            var i = 0
            val to: Int = text.length
            while (i < to) {
                i += getCodePoint(text, to, i, pCodePoints)
                val codePoint = pCodePoints[0]
                if (codePoint == '\n'.toInt()) {
                    if (drawBoxBorder) {
                        GL46.glEnd()
                        renderLineBB(lineStart, i - 1, y[0], scale, text)
                        GL46.glBegin(GL46.GL_QUADS)
                    }
                    y.put(0, y[0] + (ascent - descent + lineGap) * scale.also { lineY = it })
                    x.put(0, 0.0f)
                    lineStart = i
                    continue
                } else if (codePoint < 32 || 128 <= codePoint) {
                    continue
                }
                val cpX = x[0]
                STBTruetype.stbtt_GetBakedQuad(charData, bitmapWidth, bitmapHeight, codePoint - 32, x, y, q, true)
                x.put(0, scale(cpX, x[0], factorX))
                if (kerningEnabled && i < to) {
                    getCodePoint(text, to, i, pCodePoints)
                    x.put(
                        0,
                        x[0] + STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, codePoint, pCodePoints[0]) * scale
                    )
                }
                val x0: Float = scale(cpX, q.x0(), factorX)
                val x1: Float = scale(cpX, q.x1(), factorX)
                val y0: Float = scale(lineY, q.y0(), factorY)
                val y1: Float = scale(lineY, q.y1(), factorY)
                GL46.glTexCoord2f(q.s0(), q.t0())
                GL46.glVertex2f(x0, y0)
                GL46.glTexCoord2f(q.s1(), q.t0())
                GL46.glVertex2f(x1, y0)
                GL46.glTexCoord2f(q.s1(), q.t1())
                GL46.glVertex2f(x1, y1)
                GL46.glTexCoord2f(q.s0(), q.t1())
                GL46.glVertex2f(x0, y1)
            }
            GL46.glEnd()
            if (drawBoxBorder) {
                renderLineBB(lineStart, text.length, lineY, scale, text)
            }
        }
    }

    private fun scale(center: Float, offset: Float, factor: Float): Float {
        return (offset - center) * factor + center
    }

    private fun renderLineBB(from: Int, to: Int, y: Float, scale: Float, text: String) {
        val adjustedY = y - descent * scale
        GL46.glDisable(GL46.GL_TEXTURE_2D)
        GL46.glPolygonMode(GL46.GL_FRONT, GL46.GL_LINE)
        GL46.glColor3f(1.0f, 1.0f, 0.0f)
        val width = getStringWidth(fontInfo, text, from, to)
        GL46.glBegin(GL46.GL_QUADS)
        GL46.glVertex2f(0.0f, adjustedY)
        GL46.glVertex2f(width, adjustedY)
        GL46.glVertex2f(width, adjustedY - fontHeight)
        GL46.glVertex2f(0.0f, adjustedY - fontHeight)
        GL46.glEnd()
        GL46.glEnable(GL46.GL_TEXTURE_2D)
        GL46.glPolygonMode(GL46.GL_FRONT, GL46.GL_FILL)
        GL46.glColor3f(169f / 255f, 183f / 255f, 198f / 255f) // Text color
    }

    private fun getStringWidth(info: STBTTFontinfo, text: String, from: Int, to: Int): Float {
        var width = 0
        MemoryStack.stackPush().use { stack ->
            val pCodePoint = stack.mallocInt(1)
            val pAdvancedWidth = stack.mallocInt(1)
            val pLeftSideBearing = stack.mallocInt(1)
            var i = from
            while (i < to) {
                i += getCodePoint(text, to, i, pCodePoint)
                val cp = pCodePoint[0]
                STBTruetype.stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing)
                width += pAdvancedWidth[0]
                if (kerningEnabled && i < to) {
                    getCodePoint(text, to, i, pCodePoint)
                    width += STBTruetype.stbtt_GetCodepointKernAdvance(info, cp, pCodePoint[0])
                }
            }
        }
        return width * STBTruetype.stbtt_ScaleForPixelHeight(info, fontHeight)
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