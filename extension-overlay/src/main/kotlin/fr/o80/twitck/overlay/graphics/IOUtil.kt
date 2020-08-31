package fr.o80.twitck.overlay.graphics

import org.lwjgl.BufferUtils
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths


object IOUtil {
    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    @Throws(IOException::class)
    fun ioResourceToByteBuffer(resource: String?, bufferSize: Int): ByteBuffer {
        var buffer: ByteBuffer
        val path = Paths.get(resource)
        if (Files.isReadable(path)) {
            Files.newByteChannel(path).use { fc ->
                buffer = BufferUtils.createByteBuffer(fc.size().toInt() + 1)
                while (fc.read(buffer) != -1) {
                }
            }
        } else {
            IOUtil::class.java.classLoader.getResourceAsStream(resource).use { source ->
                Channels.newChannel(source).use { rbc ->
                    buffer = BufferUtils.createByteBuffer(bufferSize)
                    while (true) {
                        val bytes = rbc.read(buffer)
                        if (bytes == -1) {
                            break
                        }
                        if (buffer.remaining() == 0) {
                            buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2) // 50%
                        }
                    }
                }
            }
        }
        buffer.flip()
        return buffer.slice()
    }

    private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
        val newBuffer = BufferUtils.createByteBuffer(newCapacity)
        buffer.flip()
        newBuffer.put(buffer)
        return newBuffer
    }
}