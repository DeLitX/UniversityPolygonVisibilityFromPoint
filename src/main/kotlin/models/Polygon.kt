package models

data class Polygon(var edges: List<Edge> = listOf(), var points: List<Point> = listOf()) {

    init {
        val tempEdges = if (points.isNotEmpty()) {
            val list = mutableListOf<Edge>()
            for (index in 0 until points.size - 1) {
                list.add(Edge(points[index], points[index + 1]))
            }
            list.add(Edge(points.last(), points[0]))
            list
        } else {
            edges
        }
        val tempPoints = if (edges.isNotEmpty()) {
            val list = mutableListOf<Point>()
            for (edge in edges) {
                list.add(edge.point1)
            }
            list.add(edges.last().point2)
            list
        } else {
            points
        }

        //determine if polygon counterclockwise
        //https://www.element84.com/blog/determining-the-winding-of-a-polygon-given-as-a-set-of-ordered-points

        var rotationSum = 0f
        for (edge in tempEdges) {
            rotationSum += (edge.point2.x - edge.point1.x) * (edge.point2.y + edge.point1.y)
        }
        if (rotationSum < 0) {
            this.edges = tempEdges
            this.points = tempPoints
        } else {
            this.edges = tempEdges.reversed().map { Edge(it.point2, it.point1) }
            this.points = tempPoints.reversed()
        }

    }

    fun getAdjacentEdges(pointIndex: Int): Pair<Edge, Edge> =
        Pair(
            if (pointIndex == 0) {
                edges.last()
            } else {
                edges[pointIndex - 1]
            },
            edges[pointIndex]
        )

    fun getAdjacentEdges(point: Point): Pair<Edge, Edge> = getAdjacentEdges(points.indexOf(point))
}