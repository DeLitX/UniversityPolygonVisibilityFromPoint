package models

import kotlin.math.sqrt

data class Vector(
    val x: Float,
    val y: Float
) {

    constructor(point1: Point, point2: Point) : this(point2.x - point1.x, point2.y - point1.y)

    operator fun minus(vector: Vector): Vector = Vector(x - vector.x, y - vector.y)
    operator fun plus(vector: Vector): Vector = Vector(x + vector.x, y + vector.y)

    fun normalized():Vector{
        val length = length()
        return Vector(x/length,y/length)
    }
    fun length():Float{
        return sqrt(x*x+y*y)
    }
    fun dot(vector:Vector) = (x * vector.x + y * vector.y)
    fun cross(vector:Vector) = (x * vector.y - y * vector.x)
    fun collinear(vector:Vector):Boolean = (x/vector.x == y/vector.y)
}

fun Point.toVector() = Vector(x,y)