package fr.o80.twitck.overlay.graphics.ext

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL46
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@DslMarker
annotation class Drawer

@Drawer
fun draw(block: Draw.() -> Unit) {
    Draw.apply(block)
}

@Drawer
object Draw {

    @Drawer
    fun clear(red: Float, green: Float, blue: Float, alpha: Float = 1f) {
        GL46.glClearColor(red, green, blue, alpha)
    }

    @Drawer
    fun lineWidth(width: Float) {
        GL46.glLineWidth(width)
    }

    @Drawer
    fun pointSize(size: Float) {
        GL46.glPointSize(size)
    }

    @Drawer
    inline fun pushed(block: Draw.() -> Unit) {
        GL46.glPushMatrix()
        block()
        GL46.glPopMatrix()
    }

    @Drawer
    fun line(from: Vertex3f, to: Vertex3f) {
        GL46.glBegin(GL46.GL_LINES)
        GL46.glVertex3f(from.x, from.y, from.z)
        GL46.glVertex3f(to.x, to.y, to.z)
        GL46.glEnd()
    }

    @Drawer
    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        GL46.glBegin(GL46.GL_LINES)
        GL46.glVertex2f(x1, y1)
        GL46.glVertex2f(x2, y2)
        GL46.glEnd()
    }

    @Drawer
    fun quad(a: Vertex3f, b: Vertex3f, c: Vertex3f, d: Vertex3f) {
        GL46.glBegin(GL46.GL_QUADS)
        GL46.glVertex3f(a.x, a.y, a.z)
        GL46.glVertex3f(b.x, b.y, b.z)
        GL46.glVertex3f(c.x, c.y, c.z)
        GL46.glVertex3f(d.x, d.y, d.z)
        GL46.glEnd()
    }

    @Drawer
    fun translate(x: Float, y: Float, z: Float) {
        GL46.glTranslatef(x, y, z)
    }

    @Drawer
    fun scale(x: Float, y: Float, z: Float) {
        GL11.glScalef(x, y, z)
    }

    @Drawer
    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        GL46.glRotatef(angle, x, y, z)
    }

    @Drawer
    fun color(red: Float, green: Float, blue: Float) {
        GL46.glColor3f(red, green, blue)
    }

    @Drawer
    fun color(color: Vertex3f) {
        GL46.glColor3f(color.x, color.y, color.z)
    }

    @Drawer
    fun point(x: Double, y: Double, z: Double) {
        GL46.glBegin(GL46.GL_POINTS)
        GL46.glVertex3d(x, y, z)
        GL46.glEnd()
    }

    @Drawer
    fun rect(x1: Float, y1: Float, x2: Float, y2: Float) {
        line(x1, y1, x2, y1)
        line(x2, y1, x2, y2)
        line(x2, y2, x1, y2)
        line(x1, y2, x1, y1)
    }

    @Drawer
    fun quad(x1: Float, y1: Float, x2: Float, y2: Float) {
        quad(
            Vertex3f(x1, y1, 0f),
            Vertex3f(x2, y1, 0f),
            Vertex3f(x2, y2, 0f),
            Vertex3f(x1, y2, 0f),
        )
    }

    @Drawer
    inline fun texture2d(block: () -> Unit) {
        GL46.glEnable(GL46.GL_TEXTURE_2D)
        GL46.glEnable(GL46.GL_BLEND)
        block()
        GL46.glDisable(GL46.GL_BLEND)
        GL46.glDisable(GL46.GL_TEXTURE_2D)
    }

}

// Vertex 3
data class Vertex3f(val x: Float, val y: Float, val z: Float) {
    constructor(vertex2f: Vertex2f) : this(vertex2f.x, vertex2f.y, 0f)
}

// Vertex 2
data class Vertex2f(val x: Float, val y: Float) {
    fun addAngle(angle: Float, distance: Float): Vertex2f = Vertex2f(
        x = x + distance * cos(angle),
        y = y + distance * sin(angle)
    )

    infix fun vectorTo(other: Vertex2f): Vector2f = Vector2f(this, other)
    operator fun minus(from: Vertex2f): Vertex2f = Vertex2f(x - from.x, y - from.y)
    operator fun times(m: Float): Vertex2f = Vertex2f(x * m, y * m)
    operator fun plus(other: Vertex2f): Vertex2f = Vertex2f(x + other.x, y + other.y)

    infix fun distanceWith(position: Vertex2f): Float =
        sqrt((position.x - x).pow(2) + (position.y - y).pow(2))
}

data class Vertex2d(val x: Double, val y: Double)
data class Vertex2i(val x: Int, val y: Int)

// Vector 2

data class Vector2f(val from: Vertex2f, val to: Vertex2f) {

    constructor(x1: Float, y1: Float, x2: Float, y2: Float) : this(
        Vertex2f(x1, y1),
        Vertex2f(x2, y2)
    )

    val size = from distanceWith to

    val x: Float
        get() = to.x - from.x
    val y: Float
        get() = to.y - from.y

    fun collideWith(other: Vector2f): Boolean {
        val collide1 = collide(other, this)
        val collide2 = collide(this, other)
        return collide1 && collide2
    }

}
