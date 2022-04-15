package models

import kotlin.math.pow
import kotlin.math.sqrt

data class Edge(
    val point1: Point,
    val point2: Point,
) {
    fun length() = sqrt((point1.x - point2.x).pow(2) + (point1.y - point2.y).pow(2))
}