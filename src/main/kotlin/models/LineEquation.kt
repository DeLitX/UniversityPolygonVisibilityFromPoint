package models

//represents equation ax+by+c=0
data class LineEquation(
    val a: Float = 0f,
    val b: Float = 0f,
    val c: Float = 0f,
) {
    constructor(point1: Point, point2: Point) : this(
        a = point2.y - point1.y,
        b = -(point2.x - point1.x),
        c = -point1.x * (point2.y - point1.y) + point1.y * (point2.x - point1.x)
    )

    constructor(edge: Edge) : this(edge.point1, edge.point2)

    inline fun distanceToPoint(point: Point) = a * point.x + b * point.y + c

    fun intersect(line: LineEquation): Point {
        var y = -(c - line.c * a / line.a) / (b - line.b * a / line.a)
        var x = (-line.c - y * line.b) / line.a
        if (y.isNaN() || x.isNaN()) {
            y = -(line.c - c * line.a / a) / (line.b - b * line.a / a)
            x = (-c - y * b) / a
        }
        return Point(x, y)
    }

}

inline fun Point.distanceToLine(lineEquation: LineEquation) = lineEquation.distanceToPoint(this)