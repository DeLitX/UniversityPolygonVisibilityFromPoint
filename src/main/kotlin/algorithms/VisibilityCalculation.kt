package algorithms

import models.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

fun calculateVisibility(polygon: Polygon, observationPoint: Point): EdgesByVisibility {
    try {
        val shurelyInvisibleEdges = mutableListOf<Edge>()
        val maybeVisibleEdges = mutableListOf<Edge>()
        for (edge in polygon.edges) {
            val equation = LineEquation(edge)
            //https://gamedev.ru/code/forum/?id=19270
            if (equation.distanceToPoint(observationPoint) < 0) {
                shurelyInvisibleEdges.add(edge)
            } else {
                maybeVisibleEdges.add(edge)
            }
        }
        var index1 = 0
        while (index1 < maybeVisibleEdges.size) {
            val ray1 = Edge(observationPoint, maybeVisibleEdges[index1].point1)
            val ray2 = Edge(observationPoint, maybeVisibleEdges[index1].point2)
            val vector1 = Vector(ray1.point1, ray1.point2).normalized()
            val vector2 = Vector(ray2.point1, ray2.point2).normalized()
            if (vector1 == vector2) {
                shurelyInvisibleEdges.add(maybeVisibleEdges[index1])
                maybeVisibleEdges.removeAt(index1)
                continue
            }
            var point1Covered = false
            var point2Covered = false
            for (index2 in 0 until maybeVisibleEdges.size) {
                if (index1 == index2) {
                    continue
                }
                val edge = maybeVisibleEdges[index2]
                if (doIntersect(ray1, edge) && !(edge.point1 == ray1.point2 || edge.point2 == ray1.point2)) {
                    point1Covered = true
                }
                if (doIntersect(ray2, edge) && !(edge.point1 == ray2.point2 || edge.point2 == ray2.point2)) {
                    point2Covered = true
                }
                if (point1Covered && point2Covered) {
                    shurelyInvisibleEdges.add(maybeVisibleEdges[index1])
                    maybeVisibleEdges.removeAt(index1)
                    index1--
                    break
                }
            }
            if (!(point1Covered && point2Covered) && (point1Covered || point2Covered)) {
                val intersectionPoints = mutableListOf<Float>()
                val edge = maybeVisibleEdges[index1]
                for (index2 in 0 until maybeVisibleEdges.size) {
                    if (index1 == index2) {
                        continue
                    }
                    val point1 = intersectRayWithSegment(Edge(observationPoint, maybeVisibleEdges[index2].point1), edge)
                    val point2 = intersectRayWithSegment(Edge(observationPoint, maybeVisibleEdges[index2].point2), edge)
                    point1?.let {
                        intersectionPoints.add(it)
                    }
                    point2?.let {
                        intersectionPoints.add(it)
                    }
                }
                if(point2Covered){
                    val pointT = intersectionPoints.minOf { it }
                    val point = edge.point1 + (edge.point2 - edge.point1) * pointT
                    shurelyInvisibleEdges.add(Edge(point, edge.point2))
                    maybeVisibleEdges.removeAt(index1)
                    maybeVisibleEdges.add(index1, Edge(edge.point1, point))
                }else{
                    val pointT = intersectionPoints.maxOf { it }
                    val point = edge.point1 + (edge.point2 - edge.point1) * pointT
                    shurelyInvisibleEdges.add(Edge(point, edge.point1))
                    maybeVisibleEdges.removeAt(index1)
                    maybeVisibleEdges.add(index1, Edge(edge.point2, point))
                }
            }
            index1++
        }
        val result = EdgesByVisibility(maybeVisibleEdges, shurelyInvisibleEdges)
        return result
    } catch (e: Exception) {
        e.printStackTrace()
        return EdgesByVisibility(listOf(), listOf())
    }
}

fun intersectRayWithSegment(ray: Edge, segment: Edge): Float? {
    val rayLineEquation = LineEquation(ray)
    val segmentLineEquation = LineEquation(segment)
    val intersection = rayLineEquation.intersect(segmentLineEquation)

    // checking if point within the ray
    if (
        !(
                ((intersection.x - ray.point1.x).sign == (ray.point2.x - ray.point1.x).sign) &&
                        ((intersection.y - ray.point1.y).sign == (ray.point2.y - ray.point1.y).sign)
                )
    ) {
        return null
    }
    // checking if point within the segment
    if (
        !(
                ((intersection.y - segment.point1.y).sign == (segment.point2.y - segment.point1.y).sign) &&
                        ((intersection.x - segment.point1.x).sign == (segment.point2.x - segment.point1.x).sign) &&
                        ((intersection.y - segment.point2.y).sign == (segment.point1.y - segment.point2.y).sign) &&
                        ((intersection.x - segment.point2.x).sign == (segment.point1.x - segment.point2.x).sign)
                )
    ) {
        return null
    }
    if (Vector(ray.point1, ray.point2).collinear(Vector(segment.point1, segment.point2))) {
        return null
    }
    return Edge(segment.point1, intersection).length() / segment.length()
}

//https://rootllama.wordpress.com/2014/06/20/ray-line-segment-intersection-test-in-2d/
fun intersectRayWithLineSegment(ray: Edge, segment: Edge): Float? {
    val tempVector = Vector(ray.point1, ray.point2).normalized()
    return intersectRayWithLineSegment(
        ray.point1.toVector(),
        tempVector,
        segment.point1.toVector(),
        segment.point2.toVector()
    )
}

fun intersectRayWithLineSegment(o: Vector, d: Vector, a: Vector, b: Vector): Float? {
    val v1 = o - a
    val v2 = b - a
    val v3 = Vector(-d.x, d.y)
    val denom = v2.dot(v3)
    val t1 = abs(v2.cross(v1)) / denom
    val t2 = v1.dot(v3) / denom
    return if (t2 <= 0 || t2 >= 1 || t1 <= 0) {
        null
    } else {
        t2
    }
}

fun doIntersect(edge1: Edge, edge2: Edge): Boolean = doIntersect(edge1.point1, edge1.point2, edge2.point1, edge2.point2)

//https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
fun doIntersect(p1: Point, q1: Point, p2: Point, q2: Point): Boolean {
    // Find the four orientations needed for general and
    // special cases
    val o1: Int = orientation(p1, q1, p2)
    val o2: Int = orientation(p1, q1, q2)
    val o3: Int = orientation(p2, q2, p1)
    val o4: Int = orientation(p2, q2, q1)

    // General case
    if (o1 != o2 && o3 != o4) return true

    // Special Cases
    // p1, q1 and p2 are collinear and p2 lies on segment p1q1
    if (o1 == 0 && onSegment(p1, p2, q1)) return true

    // p1, q1 and q2 are collinear and q2 lies on segment p1q1
    if (o2 == 0 && onSegment(p1, q2, q1)) return true

    // p2, q2 and p1 are collinear and p1 lies on segment p2q2
    if (o3 == 0 && onSegment(p2, p1, q2)) return true

    // p2, q2 and q1 are collinear and q1 lies on segment p2q2
    if (o4 == 0 && onSegment(p2, q1, q2)) return true
    // Doesn't fall in any of the above cases
    return false
}

fun orientation(p: Point, q: Point, r: Point): Int {
    // See https://www.geeksforgeeks.org/orientation-3-ordered-points/
    // for details of below formula.
    val value = (q.y - p.y) * (r.x - q.x) -
            (q.x - p.x) * (r.y - q.y)
    if (value == 0f) return 0 // collinear
    return if (value > 0f) 1 else 2 // clock or counterclockwise
}

fun onSegment(p: Point, q: Point, r: Point): Boolean {
    return q.x <= max(p.x, r.x) && q.x >= min(p.x, r.x) &&
            q.y <= max(p.y, r.y) && q.y >= min(p.y, r.y)
}