package fr.o80.twitck.overlay.graphics.renderer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL46
import java.io.File
import javax.imageio.ImageIO

class Image(private val filename: String) {

    var id: Int = -1
        private set
    var width: Int = -1
    var height: Int = -1

    fun load() {
        if (id != -1)
            return

        // TODO Load from InputStream ?
        val bufferedImage = ImageIO.read(File(filename))
        width = bufferedImage.width
        height = bufferedImage.height

        val raw = bufferedImage.getRGB(0, 0, width, height, null, 0, width)
        val pixels = BufferUtils.createByteBuffer(width * height * 4)

        for (i in 0 until width) {
            for (j in 0 until height) {
                val pixel = raw[i * height + j]
                pixels.put(pixel.red())
                pixels.put(pixel.green())
                pixels.put(pixel.blue())
                pixels.put(pixel.alpha())
            }
        }

        pixels.flip()

        id = GL46.glGenTextures()
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, id)
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST)
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST)
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, width, height, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, pixels)
    }

    fun unload() {
        GL46.glDeleteTextures(id)
        id = -1
    }
}