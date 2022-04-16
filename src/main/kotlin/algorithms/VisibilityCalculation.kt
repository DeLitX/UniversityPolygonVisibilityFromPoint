package algorithms

import models.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

fun calculateVisibility(polygon: Polygon, observationPoint: Point): EdgesByVisibility {
    try {
        var shurelyInvisibleEdges = mutableListOf<Edge>()
        var transformedEdgesList = mutableListOf<Edge?>()
        var nearestPoint: Point = polygon.points[0]
        for (edge in polygon.edges) {
            val equation = LineEquation(edge)
            //https://gamedev.ru/code/forum/?id=19270
            if (equation.distanceToPoint(observationPoint) < 0) {
                shurelyInvisibleEdges.add(edge)
                if (transformedEdgesList.isEmpty() || transformedEdgesList.last() != null) {
                    transformedEdgesList.add(null)
                }
            } else {
                transformedEdgesList.add(edge)
            }
        }
        for (point in polygon.points) {
            if (Edge(point, observationPoint).length() <
                Edge(nearestPoint, observationPoint).length()
            ) {
                var isVisible = true
                for (edge in transformedEdgesList) {
                    if (
                        edge != null &&
                        doIntersect(Edge(observationPoint, point), edge) &&
                        !(edge.point1 == point || edge.point2 == point)
                    ) {
                        isVisible = false
                        break
                    }
                }
                if (isVisible) {
                    nearestPoint = point
                }
            }
        }

        val pair = polygon.getAdjacentEdges(nearestPoint)
        val previousEdge = if (transformedEdgesList.contains(pair.first)) {
            pair.first
        } else {
            null
        }
        val nextEdge = if (transformedEdgesList.contains(pair.second)) {
            pair.second
        } else {
            null
        }

        val moveDirection: Int
        val startEdge: Edge
        if (nextEdge != null) {
            moveDirection = 1
            startEdge = nextEdge
        } else {
            moveDirection = -1
            //it does not null because at least one of edges of the nearest point must be visible
            startEdge = previousEdge!!
        }
        val startIndex = transformedEdgesList.indexOf(startEdge)
        var currentEdgeIndex = startIndex
        var canStop = false
        var stepsAmount = 0
        while (true) {
            val edge = transformedEdgesList[currentEdgeIndex]
            if (stepsAmount >= transformedEdgesList.size && canStop) {
                break
            }
            if (edge == null) {
                val temp = removeInvisibleEdgesNearInvisibleEdge(
                    observationPoint,
                    ModifiedEdges(transformedEdgesList, shurelyInvisibleEdges, currentEdgeIndex)
                )
                transformedEdgesList = temp.transformedEdges.toMutableList()
                shurelyInvisibleEdges = temp.invisibleEdges.toMutableList()
                currentEdgeIndex = temp.index
            }
            currentEdgeIndex += moveDirection
            if (currentEdgeIndex < 0) {
                currentEdgeIndex = transformedEdgesList.lastIndex
            } else if (currentEdgeIndex > transformedEdgesList.lastIndex) {
                currentEdgeIndex = 0
            }
            canStop = true
            stepsAmount++
        }
        val visibleEdges: List<Edge> = transformedEdgesList.filterNotNull()
        val result = EdgesByVisibility(visibleEdges, shurelyInvisibleEdges)
        return result
    } catch (e: Exception) {
        e.printStackTrace()
        return EdgesByVisibility(listOf(), listOf())
    }
}

data class ModifiedEdges(val transformedEdges: List<Edge?>, val invisibleEdges: List<Edge>, val index: Int)

fun removeInvisibleEdgesNearInvisibleEdge(observationPoint: Point, oldEdges: ModifiedEdges): ModifiedEdges {
    val modifiedForward = removeNextInvisibleEdges(observationPoint, oldEdges)
    val modifiedBackward = removePreviousInvisibleEdges(observationPoint, modifiedForward)
    return modifiedBackward
}

fun removePreviousInvisibleEdges(observationPoint: Point, oldEdges: ModifiedEdges): ModifiedEdges {
    val newTransformedEdges = oldEdges.transformedEdges.toMutableList()
    val newInvisibleEdges = oldEdges.invisibleEdges.toMutableList()
    var currentIndex = oldEdges.index
    while (newTransformedEdges.isNotEmpty()) {
        val edge = newTransformedEdges[currentIndex]
        if (edge == null) {
            newTransformedEdges.removeAt(currentIndex)
            currentIndex--
            if (currentIndex == -1) {
                currentIndex = newTransformedEdges.lastIndex
            }
            continue
        }
        val isEndCovered = checkPointVisible(observationPoint, edge.point2, newTransformedEdges)
        if (!isEndCovered) {
            break
        }
        val isStartCovered = checkPointVisible(observationPoint, edge.point1, newTransformedEdges)
        if (!isStartCovered) {
            val (visibleEdge, invisibleEdge) = splitEdgeToVisibleAndInvisible(
                observationPoint,
                edge,
                isStartCovered,
                newTransformedEdges
            )
            if (invisibleEdge != null) {
                newTransformedEdges.removeAt(currentIndex)
                newTransformedEdges.add(currentIndex, visibleEdge)
                newInvisibleEdges.add(invisibleEdge)
            }
            break
        } else {
            newTransformedEdges.removeAt(currentIndex)
            newInvisibleEdges.add(edge)
            currentIndex--
        }
        if (currentIndex == -1) {
            currentIndex = newTransformedEdges.lastIndex
        }
    }
    currentIndex++
    newTransformedEdges.add(currentIndex, null)
    return ModifiedEdges(newTransformedEdges, newInvisibleEdges, currentIndex)
}

fun removeNextInvisibleEdges(observationPoint: Point, oldEdges: ModifiedEdges): ModifiedEdges {
    val newTransformedEdges = oldEdges.transformedEdges.toMutableList()
    val newInvisibleEdges = oldEdges.invisibleEdges.toMutableList()
    var currentIndex = oldEdges.index
    while (newTransformedEdges.isNotEmpty()) {
        val edge = newTransformedEdges[currentIndex]
        if (edge == null) {
            newTransformedEdges.removeAt(currentIndex)
            if (currentIndex == newTransformedEdges.size) {
                currentIndex = 0
            }
            continue
        }
        val isStartCovered = checkPointVisible(observationPoint, edge.point1, newTransformedEdges)
        if (!isStartCovered) {
            break
        }
        val isEndCovered = checkPointVisible(observationPoint, edge.point2, newTransformedEdges)
        if (!isEndCovered) {
            val (visibleEdge, invisibleEdge) = splitEdgeToVisibleAndInvisible(
                observationPoint,
                edge,
                isStartCovered,
                newTransformedEdges
            )
            if (invisibleEdge != null) {
                newTransformedEdges.removeAt(currentIndex)
                newTransformedEdges.add(currentIndex, visibleEdge)
                newInvisibleEdges.add(invisibleEdge)
            }
            break
        } else {
            newTransformedEdges.removeAt(currentIndex)
            newInvisibleEdges.add(edge)
        }
        if (currentIndex == newTransformedEdges.size) {
            currentIndex = 0
        }
    }
    newTransformedEdges.add(currentIndex, null)
    return ModifiedEdges(newTransformedEdges, newInvisibleEdges, currentIndex)
}

fun checkPointVisible(
    observationPoint: Point,
    pointToCheck: Point,
    edges: List<Edge?>
): Boolean {
    var pointCovered = false
    val ray = Edge(observationPoint, pointToCheck)
    for (edge in edges) {
        if (edge == null) {
            continue
        }
        if (doIntersect(ray, edge) && !(edge.point1 == pointToCheck || edge.point2 == pointToCheck)) {
            pointCovered = true
            break
        }
    }
    return pointCovered
}

data class SplittedEdge(val visibleEdge: Edge, val invisibleEdge: Edge?)

fun splitEdgeToVisibleAndInvisible(
    observationPoint: Point,
    edge: Edge,
    startPointVisible: Boolean,
    transformedEdges: List<Edge?>
): SplittedEdge {
    val intersectionPoints = mutableListOf<Float>()
    for (visibleEdge in transformedEdges) {
        if (visibleEdge == null) {
            continue
        }
        val ray1 = Edge(observationPoint, visibleEdge.point1)
        val ray2 = Edge(observationPoint, visibleEdge.point2)
        val point1 = intersectRayWithSegment(ray1, edge)
        val point2 = intersectRayWithSegment(ray2, edge)
        point1?.let {
            if (!doIntersect(ray1, edge)) {
                intersectionPoints.add(it)
            }
        }
        point2?.let {
            if (!doIntersect(ray2, edge)) {
                intersectionPoints.add(it)
            }
        }
    }
    if (intersectionPoints.isEmpty()) {
        return SplittedEdge(edge, null)
    }
    val pointT = intersectionPoints.minOf { it }
    val point = edge.point1 + (edge.point2 - edge.point1) * pointT
    return if (!startPointVisible) {
        SplittedEdge(visibleEdge = Edge(edge.point1, point), invisibleEdge = Edge(point, edge.point2))
    } else {
        SplittedEdge(visibleEdge = Edge(edge.point2, point), invisibleEdge = Edge(point, edge.point1))
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