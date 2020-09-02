package fr.o80.twitck.overlay.graphics.ext

import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min


const val PIF: Float = PI.toFloat()
const val TWO_PI: Double = PI * 2
const val TWO_PIF: Float = TWO_PI.toFloat()

/**
 * Map a value from a given range to another range.
 *
 * For example:
 * - 5 in [0..10] will be 0 in [-1..1]
 * - 2 in [0..3] will be ~66.67 in [0..100]
 */
fun map(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
    val fromRange = fromMax - fromMin
    val toRange = toMax - toMin
    return toMin + (value - fromMin) * toRange / fromRange
}

/**
 * Map a value from a given range to another range.
 *
 * For example:
 * - 5 in [0..10] will be 0 in [-1..1]
 * - 2 in [0..3] will be ~66.67 in [0..100]
 */
fun map(value: Double, fromMin: Double, fromMax: Double, toMin: Double, toMax: Double): Double {
    val fromRange = fromMax - fromMin
    val toRange = toMax - toMin
    return toMin + (value - fromMin) * toRange / fromRange
}

/**
 * Apply min and max to a given value.
 *
 * For example:
 * - constrain(value = 67, min = 15, max = 50) => 50
 * - constrain(value = -7, min = 15, max = 50) => 7
 */
fun constrain(value: Float, min: Float, max: Float): Float = max(min(value, max), min)

/**
 * Compute the determinant of 2 vectors.
 */
fun determinant(vector: Vector2f, point: Vertex2f): Float {
    return vector.x * (point.y - vector.from.y) - vector.y * (point.x - vector.from.x)
}

/**
 * Checks if 2 vectors collide or not.
 */
fun collide(droite: Vector2f, segment: Vector2f): Boolean {
    val detFrom = determinant(droite, segment.from)
    val detTo = determinant(droite, segment.to)
    return detFrom * detTo <= 0
}

/**
 * Compute the intersection point of 2 vectors if there is one.
 */
fun intersection(
    first: Vector2f,
    second: Vector2f,
    firstIsSegment: Boolean = false,
    secondIsSegment: Boolean = false
): Vertex2f? {

    val j = Vertex2f(second.x, second.y)

    val diviseur = first.x * second.y - first.y * second.x

    val intersection = if (diviseur != 0f) {
        val m = (first.x * first.from.y
                - first.x * second.from.y
                - first.y * first.from.x
                + first.y * second.from.x
                ) / diviseur

        second.from + j * m
    } else {
        second.from
    }

    if (firstIsSegment) {
        val firstX = first.x
        val firstY = first.y
        val x = intersection.x - first.from.x
        val y = intersection.y - first.from.y

        if (x !in range(firstX) || y !in range(firstY)) {
            return null
        }
    }

    if (secondIsSegment) {
        val secondX = second.x
        val secondY = second.y
        val x = intersection.x - second.from.x
        val y = intersection.y - second.from.y

        if (x !in range(secondX) || y !in range(secondY)) {
            return null
        }
    }

    return intersection
}

/**
 * Return a range between 0 and a given value, no matter if the value is bigger than zero or not.
 *
 * For example:
 * - range(5) = 0..5
 * - range(-5) = -5..0
 */
private fun range(limit: Float): ClosedRange<Float> {
    return if (limit < 0) {
        limit..0f
    } else {
        0f..limit
    }
}