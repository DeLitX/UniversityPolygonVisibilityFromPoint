package models

import androidx.compose.ui.geometry.Offset

data class Point(
    val x: Float = 0.0f,
    val y: Float = 0.0f,
) {
    fun toOffset(): Offset = Offset(x, y)
    operator fun minus(point: Point) = Point(x - point.x, y - point.y)
    operator fun plus(point: Point) = Point(x + point.x, y + point.y)
    operator fun times(value: Float) = Point(x * value, y * value)

}

operator fun Float.times(point: Point) = Point(point.x * this, point.y * this)
fun Offset.toPoint() = Point(x, y)
